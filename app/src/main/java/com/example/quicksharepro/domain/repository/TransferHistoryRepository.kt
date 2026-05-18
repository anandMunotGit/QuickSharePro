package com.example.quicksharepro.domain.repository

import com.example.quicksharepro.domain.model.TransferSession
import kotlinx.coroutines.flow.Flow

interface TransferHistoryRepository {
    fun getHistory(): Flow<List<TransferSession>>
    suspend fun saveTransfer(session: TransferSession)
    suspend fun deleteTransfer(sessionId: String)
    suspend fun clearHistory()
}
