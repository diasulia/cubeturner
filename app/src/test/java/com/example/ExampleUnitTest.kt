package com.example

import com.example.data.AdbCrypto
import com.example.data.AdbTcpClient
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.Signature
import java.security.interfaces.RSAPublicKey

class ExampleUnitTest {

    @Test
    fun base64RoundTrip_preservesAllByteLengths() {
        for (len in listOf(0, 1, 2, 3, 20, 256, 524)) {
            val data = ByteArray(len) { idx -> ((idx * 37 + 13) and 0xFF).toByte() }
            val encoded = AdbCrypto.encodeBase64(data)
            val decoded = AdbCrypto.decodeBase64(encoded)
            assertArrayEquals("Mismatch for length $len", data, decoded)
        }
    }

    @Test
    fun adbRsaStruct_hasExact524BytesAndValidMontgomeryInverse() {
        val kp = AdbCrypto.generateKeyPair()
        val pub = kp.public as RSAPublicKey
        val struct = AdbCrypto.buildAndroidRsaStruct(pub)
        assertEquals(524, struct.size)

        val buf = ByteBuffer.wrap(struct).order(ByteOrder.LITTLE_ENDIAN)
        val words = buf.int
        val n0invUnsigned = buf.int.toLong() and 0xFFFFFFFFL
        assertEquals(64, words)

        // Verify Android libcrypto_utils invariant: (n * n0inv) mod 2^32 == 2^32 - 1
        val r32 = BigInteger.ONE.shiftLeft(32)
        val productMod = pub.modulus.mod(r32)
            .multiply(BigInteger.valueOf(n0invUnsigned))
            .mod(r32)
        assertEquals(r32.subtract(BigInteger.ONE), productMod)

        // Verify restoreKeyPair round-trip
        val restored = AdbCrypto.restoreKeyPair(
            AdbCrypto.encodePublicKeyX509(kp),
            AdbCrypto.encodePrivateKeyPkcs8(kp)
        )
        assertNotNull(restored)

        // Verify SHA-1 token signature validates with SHA1withRSA over a message whose SHA-1 matches token
        val message = "magcubic-adb-auth-test".toByteArray()
        val sha1 = java.security.MessageDigest.getInstance("SHA-1").digest(message)
        val sigBytes = AdbCrypto.signToken(kp.private, sha1)
        assertEquals(256, sigBytes.size)

        val verifier = Signature.getInstance("SHA1withRSA")
        verifier.initVerify(kp.public)
        verifier.update(message)
        assertTrue(verifier.verify(sigBytes))
    }

    @Test
    fun adbPacketSerialization_setsLittleEndianMagicAndChecksum() {
        val payload = "host::\u0000".toByteArray(Charsets.UTF_8)
        val packet = AdbTcpClient.serializePacket(
            command = AdbTcpClient.CMD_CNXN,
            arg0 = 0x01000000,
            arg1 = 65536,
            payload = payload
        )
        assertEquals(24 + payload.size, packet.size)
        val buf = ByteBuffer.wrap(packet).order(ByteOrder.LITTLE_ENDIAN)
        assertEquals(AdbTcpClient.CMD_CNXN, buf.int)
        assertEquals(0x01000000, buf.int)
        assertEquals(65536, buf.int)
        assertEquals(payload.size, buf.int)
        assertEquals(AdbTcpClient.computePayloadChecksum(payload), buf.int)
        assertEquals(AdbTcpClient.CMD_CNXN.inv(), buf.int)
    }
}
