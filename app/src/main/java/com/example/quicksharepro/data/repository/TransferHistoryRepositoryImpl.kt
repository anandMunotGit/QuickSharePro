package com.example.quicksharepro.data.repository

import com.example.quicksharepro.data.local.TransferDao
import com.example.quicksharepro.data.local.TransferEntity
import com.example.quicksharepro.data.local.TransferFileEntity
import com.example.quicksharepro.domain.model.DeviceInfo
import com.example.quicksharepro.domain.model.TransferFile
import com.example.quicksharepro.domain.model.TransferSession
import com.example.quicksharepro.domain.repository.TransferHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferHistoryRepositoryImpl @Inject constructor(
    private val dao: TransferDao
) : TransferHistoryRepository {

    override fun getHistory(): Flow<List<TransferSession>> {
        return dao.getAllTransfers().map { entities ->
            entities.map { entity ->
                TransferSession(
                    sessionId = entity.sessionId,
                    peerDevice = DeviceInfo(entity.deviceName, entity.deviceAddress),
                    files = emptyList(), // Simplified: files would need a separate join query
                    status = entity.status,
                    startTime = entity.startTime,
                    _totalSize = entity.totalSize
                )
            }
        }
    }

    override suspend fun saveTransfer(session: TransferSession) {
        val entity = TransferEntity(
            sessionId = session.sessionId,
            deviceName = session.peerDevice.deviceName,
            deviceAddress = session.peerDevice.deviceAddress,
            status = session.status,
            startTime = session.startTime,
            totalSize = session.totalSize,
            isIncoming = false // Default
        )
        dao.insertTransfer(entity)
        
        val fileEntities = session.files.map { file ->
            TransferFileEntity(
                sessionId = session.sessionId,
                fileName = file.name,
                fileSize = file.size,
                mimeType = file.mimeType,
                uri = file.uri?.toString(),
                localPath = file.path
            )
        }
        dao.insertFiles(fileEntities)
    }

    override suspend fun deleteTransfer(sessionId: String) {
        dao.deleteTransfer(sessionId)
    }

    override suspend fun clearHistory() {
        dao.clearHistory()
    }
}
