package com.example.quicksharepro.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransferDao {
    @Query("SELECT * FROM transfers ORDER BY startTime DESC")
    fun getAllTransfers(): Flow<List<TransferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<TransferFileEntity>)

    @Query("UPDATE transfers SET status = :status WHERE sessionId = :sessionId")
    suspend fun updateTransferStatus(sessionId: String, status: String)

    @Query("UPDATE transfer_files SET bytesTransferred = :bytes WHERE sessionId = :sessionId AND fileName = :fileName")
    suspend fun updateFileProgress(sessionId: String, fileName: String, bytes: Long)

    @Query("DELETE FROM transfers WHERE sessionId = :sessionId")
    suspend fun deleteTransfer(sessionId: String)

    @Query("DELETE FROM transfers")
    suspend fun clearHistory()
}
