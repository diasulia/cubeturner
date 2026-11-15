package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey
import java.util.concurrent.atomic.AtomicInteger

/**
 * Direct TCP/IP implementation of the Android ADB wire protocol (port 5555).
 * Connects directly from an Android phone on the same Wi-Fi LAN to the Magcubic projector's adbd.
 */
class AdbTcpClient {

    companion object {
        const val CMD_CNXN = 0x4e584e43 // "CNXN"
        const val CMD_AUTH = 0x48545541 // "AUTH"
        const val CMD_OPEN = 0x4e45504f // "OPEN"
        const val CMD_OKAY = 0x59414b4f // "OKAY"
        const val CMD_WRTE = 0x45545257 // "WRTE"
        const val CMD_CLSE = 0x45534c43 // "CLSE"

        private const val ADB_VERSION = 0x01000000
        private const val MAX_DATA = 65536

        private const val AUTH_TYPE_TOKEN = 1
        private const val AUTH_TYPE_SIGNATURE = 2
        private const val AUTH_TYPE_RSAPUBLICKEY = 3

        internal fun computePayloadChecksum(data: ByteArray): Int {
            var sum = 0L
            for (b in data) {
                sum += (b.toInt() and 0xFF)
            }
            return (sum and 0xFFFFFFFFL).toInt()
        }

        internal fun serializePacket(
            command: Int,
            arg0: Int,
            arg1: Int,
            payload: ByteArray = ByteArray(0)
        ): ByteArray {
            val header = ByteBuffer.allocate(24 + payload.size).order(ByteOrder.LITTLE_ENDIAN)
            header.putInt(command)
            header.putInt(arg0)
            header.putInt(arg1)
            header.putInt(payload.size)
            header.putInt(computePayloadChecksum(payload))
            header.putInt(command.inv())
            if (payload.isNotEmpty()) {
                header.put(payload)
            }
            return header.array()
        }
    }

    internal data class AdbPacket(
        val command: Int,
        val arg0: Int,
        val arg1: Int,
        val payload: ByteArray
    )

    private val mutex = Mutex()
    private val nextLocalId = AtomicInteger(1)
    private var socket: Socket? = null
    private var input: DataInputStream? = null
    private var output: OutputStream? = null

    val isConnected: Boolean
        get() = socket?.let { it.isConnected && !it.isClosed } == true

    suspend fun connect(
        host: String,
        port: Int,
        keyPair: KeyPair,
        onWaitingForProjectorPrompt: () -> Unit
    ): Result<String> = withContext(Dispatchers.IO) {
        mutex.withLock {
            closeInternal()
            try {
                val cleanHost = host.trim()
                require(cleanHost.isNotEmpty()) { "Informe o endereço IP do projetor." }

                val sock = Socket()
                sock.tcpNoDelay = true
                sock.connect(InetSocketAddress(cleanHost, port), 5_500)
                sock.soTimeout = 7_500

                val ins = DataInputStream(sock.getInputStream())
                val outs = sock.getOutputStream()

                socket = sock
                input = ins
                output = outs

                // 1. Send CNXN
                val bannerPayload = "host::\u0000".toByteArray(Charsets.UTF_8)
                writePacket(outs, CMD_CNXN, ADB_VERSION, MAX_DATA, bannerPayload)

                var pkt = readPacket(ins)
                if (pkt.command == CMD_AUTH && pkt.arg0 == AUTH_TYPE_TOKEN) {
                    // Try signing the token with our persisted RSA-2048 key first
                    val signature = AdbCrypto.signToken(keyPair.private, pkt.payload)
                    writePacket(outs, CMD_AUTH, AUTH_TYPE_SIGNATURE, 0, signature)
                    pkt = readPacket(ins)

                    if (pkt.command == CMD_AUTH && pkt.arg0 == AUTH_TYPE_TOKEN) {
                        // Projector hasn't trusted our RSA public key yet -> send public key & wait for remote click!
                        onWaitingForProjectorPrompt()
                        sock.soTimeout = 45_000
                        val pubKeyPayload = AdbCrypto.encodeAdbPublicKeyPayload(
                            keyPair.public as RSAPublicKey
                        )
                        writePacket(outs, CMD_AUTH, AUTH_TYPE_RSAPUBLICKEY, 0, pubKeyPayload)
                        pkt = readPacket(ins)
                        sock.soTimeout = 8_000
                    }
                }

                if (pkt.command != CMD_CNXN) {
                    closeInternal()
                    return@withLock Result.failure(
                        IllegalStateException("Conexão recusada pelo projetor (código ${pkt.command}). Verifique se clicou em 'Permitir' na tela.")
                    )
                }

                val deviceBanner = pkt.payload.toString(Charsets.UTF_8).trimEnd('\u0000')
                Result.success(deviceBanner)
            } catch (_: SocketTimeoutException) {
                closeInternal()
                Result.failure(
                    IllegalStateException(
                        "Tempo esgotado ao conectar em $host:$port. Confirme se ambos estão no mesmo Wi-Fi, se a Depuração USB está ativa e se aceitou o aviso na tela do projetor."
                    )
                )
            } catch (e: Exception) {
                closeInternal()
                Result.failure(
                    IllegalStateException(
                        "Não foi possível conectar em $host:$port (${e.localizedMessage ?: "erro de rede"})."
                    )
                )
            }
        }
    }

    suspend fun execShell(command: String): Result<String> = withContext(Dispatchers.IO) {
        mutex.withLock {
            val sock = socket
            val ins = input
            val outs = output
            if (sock == null || ins == null || outs == null || sock.isClosed) {
                return@withLock Result.failure(IllegalStateException("Projetor desconectado."))
            }

            try {
                sock.soTimeout = 10_000
                val localId = nextLocalId.getAndIncrement()
                val openPayload = "shell:$command\u0000".toByteArray(Charsets.UTF_8)
                writePacket(outs, CMD_OPEN, localId, 0, openPayload)

                val outBuffer = ByteArrayOutputStream()
                var remoteId = 0

                while (true) {
                    val pkt = readPacket(ins)
                    when (pkt.command) {
                        CMD_OKAY -> {
                            if (pkt.arg1 == localId) {
                                remoteId = pkt.arg0
                            }
                        }
                        CMD_WRTE -> {
                            if (pkt.arg1 == localId) {
                                remoteId = pkt.arg0
                                if (pkt.payload.isNotEmpty()) {
                                    outBuffer.write(pkt.payload)
                                }
                                writePacket(outs, CMD_OKAY, localId, remoteId)
                            }
                        }
                        CMD_CLSE -> {
                            if (pkt.arg1 == localId || pkt.arg1 == 0) {
                                writePacket(outs, CMD_CLSE, localId, remoteId)
                                break
                            }
                        }
                    }
                }
                Result.success(outBuffer.toString(Charsets.UTF_8.name()).trim())
            } catch (e: Exception) {
                closeInternal()
                Result.failure(
                    IllegalStateException("Conexão ADB interrompida: ${e.localizedMessage ?: "falha de leitura"}")
                )
            }
        }
    }

    suspend fun disconnect() = withContext(Dispatchers.IO) {
        mutex.withLock {
            closeInternal()
        }
    }

    private fun closeInternal() {
        try {
            socket?.close()
        } catch (_: Exception) {
        } finally {
            socket = null
            input = null
            output = null
        }
    }

    private fun writePacket(
        outs: OutputStream,
        command: Int,
        arg0: Int,
        arg1: Int,
        payload: ByteArray = ByteArray(0)
    ) {
        outs.write(serializePacket(command, arg0, arg1, payload))
        outs.flush()
    }

    private fun readPacket(ins: DataInputStream): AdbPacket {
        val headerBytes = ByteArray(24)
        ins.readFully(headerBytes)
        val header = ByteBuffer.wrap(headerBytes).order(ByteOrder.LITTLE_ENDIAN)
        val command = header.int
        val arg0 = header.int
        val arg1 = header.int
        val dataLength = header.int
        header.int // checksum
        header.int // magic

        val safeLength = dataLength.coerceIn(0, 1024 * 1024)
        val payload = ByteArray(safeLength)
        if (safeLength > 0) {
            ins.readFully(payload)
        }
        return AdbPacket(command, arg0, arg1, payload)
    }
}
