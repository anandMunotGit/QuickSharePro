package com.example.quicksharepro.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.quicksharepro.domain.model.TransferStatus

@Entity(tableName = "transfers")
data class TransferEntity(
    @PrimaryKey
    val sessionId: String,
    val deviceName: String,
    val deviceAddress: String,
    val status: TransferStatus,
    val startTime: Long,
    val totalSize: Long,
    val isIncoming: Boolean
)
