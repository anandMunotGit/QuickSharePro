package com.example.quicksharepro.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class TransferWithFiles(
    @Embedded val transfer: TransferEntity,
    @Relation(
        parentColumn = "sessionId",
        entityColumn = "sessionId"
    )
    val files: List<TransferFileEntity>
)
