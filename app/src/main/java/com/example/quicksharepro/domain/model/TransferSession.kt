package com.example.quicksharepro.domain.model

data class TransferSession(
    val sessionId: String,
    val peerDevice: DeviceInfo,
    val files: List<TransferFile>,
    val status: TransferStatus,
    val startTime: Long = System.currentTimeMillis(),
    val _totalSize: Long? = null,
    val isReceiver: Boolean = false,
    val errorMessage: String? = null
) {
    val totalSize: Long
        get() = _totalSize ?: files.sumOf { it.size }
}
