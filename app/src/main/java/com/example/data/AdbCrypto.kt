package com.example.data

import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.Signature
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

/**
 * Implements Android ADB RSA-2048 cryptography:
 * - Keypair generation & serialization
 * - ADB_AUTH_SIGNATURE (SHA-1 DigestInfo + NONEwithRSA)
 * - ADB_AUTH_RSAPUBLICKEY (524-byte Android libcrypto_utils RSAPublicKey struct)
 */
object AdbCrypto {

    private const val KEY_BITS = 2048
    private const val KEY_WORDS = KEY_BITS / 32 // 64 uint32 words
    private const val KEY_BYTES = KEY_BITS / 8  // 256 bytes

    // ASN.1 DER DigestInfo prefix for SHA-1 (15 bytes + 20-byte hash = 35 bytes)
    private val SHA1_DIGEST_INFO_PREFIX = byteArrayOf(
        0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2b, 0x0e,
        0x03, 0x02, 0x1a, 0x05, 0x00, 0x04, 0x14
    )

    fun generateKeyPair(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(KEY_BITS)
        return generator.generateKeyPair()
    }

    fun restoreKeyPair(pubBase64: String, privBase64: String): KeyPair? {
        return try {
            val kf = KeyFactory.getInstance("RSA")
            val pub = kf.generatePublic(X509EncodedKeySpec(decodeBase64(pubBase64)))
            val priv = kf.generatePrivate(PKCS8EncodedKeySpec(decodeBase64(privBase64)))
            KeyPair(pub, priv)
        } catch (_: Exception) {
            null
        }
    }

    fun encodePublicKeyX509(keyPair: KeyPair): String = encodeBase64(keyPair.public.encoded)

    fun encodePrivateKeyPkcs8(keyPair: KeyPair): String = encodeBase64(keyPair.private.encoded)

    /**
     * Signs the 20-byte token sent by adbd in ADB_AUTH_TOKEN.
     */
    fun signToken(privateKey: PrivateKey, token: ByteArray): ByteArray {
        val payload = ByteArray(SHA1_DIGEST_INFO_PREFIX.size + token.size)
        System.arraycopy(SHA1_DIGEST_INFO_PREFIX, 0, payload, 0, SHA1_DIGEST_INFO_PREFIX.size)
        System.arraycopy(token, 0, payload, SHA1_DIGEST_INFO_PREFIX.size, token.size)

        val signer = Signature.getInstance("NONEwithRSA")
        signer.initSign(privateKey)
        signer.update(payload)
        return signer.sign()
    }

    /**
     * Builds the 524-byte Android RSAPublicKey binary structure and formats it as
     * `<base64> <identity>\0` for ADB_AUTH_RSAPUBLICKEY.
     */
    fun encodeAdbPublicKeyPayload(
        publicKey: RSAPublicKey,
        identity: String = "cubetuner@android"
    ): ByteArray {
        val structBytes = buildAndroidRsaStruct(publicKey)
        val base64Key = encodeBase64(structBytes)
        return "$base64Key $identity\u0000".toByteArray(Charsets.UTF_8)
    }

    internal fun buildAndroidRsaStruct(publicKey: RSAPublicKey): ByteArray {
        val n = publicKey.modulus
        val r32 = BigInteger.ONE.shiftLeft(32) // 2^32
        val r = BigInteger.ONE.shiftLeft(KEY_BITS) // 2^2048
        val rr = r.multiply(r).mod(n) // (2^2048)^2 mod n

        // n0inv = -(n^-1 mod 2^32) mod 2^32
        val nModR32 = n.mod(r32)
        val nInv = nModR32.modInverse(r32)
        val n0inv = r32.subtract(nInv).mod(r32).toLong() and 0xFFFFFFFFL

        val modulusLe = bigIntToLittleEndianFixed(n, KEY_BYTES)
        val rrLe = bigIntToLittleEndianFixed(rr, KEY_BYTES)

        val buffer = ByteBuffer.allocate(4 + 4 + KEY_BYTES + KEY_BYTES + 4)
            .order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(KEY_WORDS)
        buffer.putInt(n0inv.toInt())
        buffer.put(modulusLe)
        buffer.put(rrLe)
        buffer.putInt(publicKey.publicExponent.toInt())
        return buffer.array()
    }

    private fun bigIntToLittleEndianFixed(value: BigInteger, sizeBytes: Int): ByteArray {
        val be = value.toByteArray()
        val result = ByteArray(sizeBytes)
        // Strip optional leading 0x00 sign byte from BigInteger two's complement
        val startIdx = if (be.size > sizeBytes && be[0] == 0.toByte()) be.size - sizeBytes else 0
        val copyLen = minOf(be.size - startIdx, sizeBytes)
        for (i in 0 until copyLen) {
            result[i] = be[be.size - 1 - i]
        }
        return result
    }

    // Pure-Kotlin RFC 4648 Base64 (no line wraps, API 24 + JVM unit test compatible)
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"

    fun encodeBase64(data: ByteArray): String {
        val sb = StringBuilder(((data.size + 2) / 3) * 4)
        var i = 0
        while (i < data.size) {
            val b0 = data[i].toInt() and 0xFF
            val b1 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else 0
            val triple = (b0 shl 16) or (b1 shl 8) or b2

            sb.append(ALPHABET[(triple ushr 18) and 0x3F])
            sb.append(ALPHABET[(triple ushr 12) and 0x3F])
            sb.append(if (i + 1 < data.size) ALPHABET[(triple ushr 6) and 0x3F] else '=')
            sb.append(if (i + 2 < data.size) ALPHABET[triple and 0x3F] else '=')
            i += 3
        }
        return sb.toString()
    }

    fun decodeBase64(encoded: String): ByteArray {
        val clean = encoded.trim().filter { !it.isWhitespace() }
        if (clean.isEmpty()) return ByteArray(0)
        val lookup = IntArray(128) { -1 }
        for (idx in ALPHABET.indices) lookup[ALPHABET[idx].code] = idx

        val pad = clean.takeLast(2).count { it == '=' }
        val outLen = (clean.length * 3) / 4 - pad
        val out = ByteArray(outLen)
        var outIdx = 0
        var i = 0
        while (i + 3 < clean.length) {
            val c0 = lookup[clean[i].code and 0x7F].coerceAtLeast(0)
            val c1 = lookup[clean[i + 1].code and 0x7F].coerceAtLeast(0)
            val c2 = if (clean[i + 2] == '=') 0 else lookup[clean[i + 2].code and 0x7F].coerceAtLeast(0)
            val c3 = if (clean[i + 3] == '=') 0 else lookup[clean[i + 3].code and 0x7F].coerceAtLeast(0)
            val triple = (c0 shl 18) or (c1 shl 12) or (c2 shl 6) or c3
            if (outIdx < outLen) out[outIdx++] = ((triple ushr 16) and 0xFF).toByte()
            if (outIdx < outLen) out[outIdx++] = ((triple ushr 8) and 0xFF).toByte()
            if (outIdx < outLen) out[outIdx++] = (triple and 0xFF).toByte()
            i += 4
        }
        return out
    }
}
