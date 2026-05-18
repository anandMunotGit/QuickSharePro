package com.example.quicksharepro.data.transfer

import android.content.Context
import android.net.Uri
import android.os.Build
import com.example.quicksharepro.data.local.SettingsPreferences
import com.example.quicksharepro.domain.model.TransferProgress
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsPreferences
) {
    private val _progress = MutableStateFlow<TransferProgress?>(null)
    val progress = _progress.asStateFlow()

    private val CHUNK_SIZE = 1024 * 1024 // 1MB

    suspend fun sendFile(
        socket: Socket,
        uri: Uri,
        fileName: String,
        fileSize: Long,
        sessionId: String,
        fileIndex: Int,
        totalAlreadyTransferred: Long,
        sessionTotalSize: Long
    ) = withContext(Dispatchers.IO) {
        val outputStream = socket.getOutputStream()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext
        
        val buffer = ByteArray(CHUNK_SIZE)
        var bytesRead: Int
        var totalBytesSent = 0L
        val startTime = System.currentTimeMillis()

        inputStream.use { input ->
            while (input.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                totalBytesSent += bytesRead
                
                updateProgress(sessionId, fileIndex, totalBytesSent, fileSize, startTime, totalAlreadyTransferred, sessionTotalSize)
            }
        }
        outputStream.flush()
    }

    suspend fun receiveFile(
        socket: Socket,
        fileName: String,
        fileSize: Long,
        sessionId: String,
        fileIndex: Int,
        totalAlreadyTransferred: Long,
        sessionTotalSize: Long
    ) = withContext(Dispatchers.IO) {
        android.util.Log.d("TransferEngine", "Starting to receive file: $fileName (Size: $fileSize bytes)")
        
        val inputStream = socket.getInputStream()
        
        val resolver = context.contentResolver
        val mimeType = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(File(fileName).extension) ?: "application/octet-stream"
        val isImage = mimeType.startsWith("image/")
        val isVideo = mimeType.startsWith("video/")
        val galleryEnabled = settings.saveToGallery

        val contentValues = android.content.ContentValues().apply {
            put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.MediaColumns.SIZE, fileSize)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val relativePath = when {
                    galleryEnabled && isImage -> android.os.Environment.DIRECTORY_PICTURES + "/QuickSharePro"
                    galleryEnabled && isVideo -> android.os.Environment.DIRECTORY_MOVIES + "/QuickSharePro"
                    else -> android.os.Environment.DIRECTORY_DOWNLOADS + "/QuickSharePro"
                }
                put(android.provider.MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(android.provider.MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            when {
                galleryEnabled && isImage -> android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                galleryEnabled && isVideo -> android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                else -> android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }
        } else {
            if (isImage) {
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            } else if (isVideo) {
                android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                android.provider.MediaStore.Files.getContentUri("external")
            }
        }

        val uri = resolver.insert(collection, contentValues)
        if (uri == null) {
            android.util.Log.e("TransferEngine", "Failed to create MediaStore entry for $fileName")
            throw Exception("Failed to provision local file storage.")
        }
        
        val outputStream = resolver.openOutputStream(uri) 
            ?: throw Exception("Failed to open output stream to MediaStore.")
            
        val buffer = ByteArray(CHUNK_SIZE)
        var bytesRead: Int
        var totalBytesReceived = 0L
        val startTime = System.currentTimeMillis()

        try {
            outputStream.use { output ->
                while (totalBytesReceived < fileSize) {
                    val remaining = (fileSize - totalBytesReceived).coerceAtMost(CHUNK_SIZE.toLong()).toInt()
                    bytesRead = inputStream.read(buffer, 0, remaining)
                    if (bytesRead == -1) {
                        android.util.Log.e("TransferEngine", "Socket closed prematurely while receiving file!")
                        break
                    }
                    
                    output.write(buffer, 0, bytesRead)
                    totalBytesReceived += bytesRead
                    
                    updateProgress(sessionId, fileIndex, totalBytesReceived, fileSize, startTime, totalAlreadyTransferred, sessionTotalSize)
                }
            }
        } finally {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(android.provider.MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
        }
    }

    private fun updateProgress(
        sessionId: String,
        fileIndex: Int,
        transferredInFile: Long,
        totalInFile: Long,
        startTime: Long,
        totalAlreadyTransferred: Long,
        sessionTotalSize: Long
    ) {
        val totalTransferred = totalAlreadyTransferred + transferredInFile
        val elapsed = (System.currentTimeMillis() - startTime).coerceAtLeast(1)
        val speed = (transferredInFile.toDouble() / elapsed) * 1000 // Bytes per second
        
        val totalRemaining = (sessionTotalSize - totalTransferred).coerceAtLeast(0L)
        val eta = if (speed > 0) (totalRemaining / speed * 1000).toLong() else 0L

        _progress.value = TransferProgress(
            sessionId = sessionId,
            currentFileIndex = fileIndex,
            bytesTransferredInCurrentFile = transferredInFile,
            totalBytesTransferred = totalTransferred,
            currentSpeed = speed,
            estimatedTimeRemaining = eta
        )
    }
}
