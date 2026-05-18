package com.example.quicksharepro.domain.model

enum class TransferStatus {
    PENDING,
    CONNECTING,
    REQUESTED_CONNECTION,
    AWAITING_AUTHORIZATION,
    TRANSFERRING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}
