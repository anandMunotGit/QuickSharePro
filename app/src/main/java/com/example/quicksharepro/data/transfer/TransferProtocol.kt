package com.example.quicksharepro.data.transfer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
sealed class TransferMessage {
    @Serializable
    data class ConnectionRequest(val deviceName: String) : TransferMessage()

    @Serializable
    data class ConnectionResponse(val accepted: Boolean) : TransferMessage()

    @Serializable
    data class Handshake(val sessionId: String, val deviceName: String) : TransferMessage()

    @Serializable
    data class FileMetadata(
        val name: String,
        val size: Long,
        val mimeType: String,
        val chunkCount: Int,
        val index: Int
    ) : TransferMessage()

    @Serializable
    data class ChunkAck(val chunkIndex: Int) : TransferMessage()

    @Serializable
    data class TransferComplete(val sessionId: String) : TransferMessage()

    @Serializable
    data class TransferError(val message: String) : TransferMessage()
}

object TransferProtocol {
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(message: TransferMessage): String {
        return json.encodeToString(TransferMessage.serializer(), message)
    }

    fun decode(line: String): TransferMessage {
        return json.decodeFromString(TransferMessage.serializer(), line)
    }
}
