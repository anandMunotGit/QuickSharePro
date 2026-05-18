package com.example.quicksharepro.domain.model

data class TransferProgress(
    val sessionId: String,
    val currentFileIndex: Int,
    val bytesTransferredInCurrentFile: Long,
    val totalBytesTransferred: Long,
    val currentSpeed: Double, // Bytes per second
    val estimatedTimeRemaining: Long // Milliseconds
)
