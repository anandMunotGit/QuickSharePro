package com.example.quicksharepro.data.local

import androidx.room.TypeConverter
import com.example.quicksharepro.domain.model.TransferStatus

class Converters {
    @TypeConverter
    fun fromTransferStatus(status: TransferStatus): String {
        return status.name
    }

    @TypeConverter
    fun toTransferStatus(status: String): TransferStatus {
        return TransferStatus.valueOf(status)
    }
}
