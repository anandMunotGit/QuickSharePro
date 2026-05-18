package com.example.quicksharepro.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TransferEntity::class, TransferFileEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class QuickShareDatabase : RoomDatabase() {
    abstract val transferDao: TransferDao
}
