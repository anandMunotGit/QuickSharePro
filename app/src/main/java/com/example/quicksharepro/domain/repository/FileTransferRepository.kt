package com.example.quicksharepro.domain.repository

import com.example.quicksharepro.domain.model.TransferFile
import com.example.quicksharepro.domain.model.TransferProgress
import com.example.quicksharepro.domain.model.TransferSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface FileTransferRepository {
    val currentTransfer: StateFlow<TransferSession?>
    val transferProgress: Flow<TransferProgress?>

    suspend fun startSending(files: List<TransferFile>)
    suspend fun startReceiving()
    suspend fun connectToSenderDirectly(ip: String)
    fun acceptConnection(sessionId: String)
    fun rejectConnection(sessionId: String)
    suspend fun pauseTransfer(sessionId: String)
    suspend fun resumeTransfer(sessionId: String)
    suspend fun cancelTransfer(sessionId: String)
    fun clearTransferState()
}
