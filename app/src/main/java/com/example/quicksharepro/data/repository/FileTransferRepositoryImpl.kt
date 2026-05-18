package com.example.quicksharepro.data.repository

import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.quicksharepro.data.transfer.*
import com.example.quicksharepro.data.wifi.WifiDirectManager
import com.example.quicksharepro.domain.model.*
import com.example.quicksharepro.domain.repository.FileSelectionRepository
import com.example.quicksharepro.domain.repository.FileTransferRepository
import com.example.quicksharepro.service.FileTransferService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Socket
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileTransferRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val socketServer: SocketServer,
    private val socketClient: SocketClient,
    private val transferEngine: TransferEngine,
    private val wifiDirectManager: WifiDirectManager,
    private val selectionRepository: FileSelectionRepository
) : FileTransferRepository {

    private var pendingAuthorization: kotlinx.coroutines.CompletableDeferred<Boolean>? = null
    private val repositoryScope = CoroutineScope(Dispatchers.IO)
    private val _currentTransfer = MutableStateFlow<TransferSession?>(null)
    override val currentTransfer = _currentTransfer.asStateFlow()

    private val _transferProgress = MutableStateFlow<TransferProgress?>(null)
    override val transferProgress = _transferProgress.asStateFlow()

    private val TRANSFER_PORT = 8888
    private var pendingFiles: List<TransferFile>? = null

    init {
        // Observe transfer progress
        repositoryScope.launch {
            transferEngine.progress.collect { progress ->
                _transferProgress.value = progress
            }
        }

        // Handle OS-assigned P2P Roles
        repositoryScope.launch {
            wifiDirectManager.connectionInfo
                .collect { info ->
                    if (info == "Disconnected") return@collect

                    if (info == "Group Owner (Server)") {
                        socketServer.startListening(TRANSFER_PORT) { socket ->
                            processSocket(socket)
                        }
                    } else if (info == "Group Client") {
                        kotlinx.coroutines.delay(1000)
                        val peerAddress = wifiDirectManager.groupOwnerAddress.value
                        if (peerAddress != null) {
                            val socket = socketClient.connect(peerAddress, TRANSFER_PORT)
                            if (socket != null) {
                                processSocket(socket)
                            } else {
                                android.util.Log.e("Transfer", "Failed to connect to GO via TCP.")
                            }
                        }
                    }
                }
        }
    }

    private fun processSocket(socket: Socket) {
        val files = pendingFiles
        if (files != null && files.isNotEmpty()) {
            handleOutgoingTransfer(socket, files)
        } else {
            handleIncomingTransfer(socket)
        }
    }

    private fun startService() {
        val intent = Intent(context, FileTransferService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    override suspend fun startSending(files: List<TransferFile>) {
        _currentTransfer.value = null // Clear old state
        pendingFiles = files
        
        // Deep reset
        socketServer.stop()
        _transferProgress.value = null
        
        // Remove any existing group before creating a new one
        wifiDirectManager.disconnect()
        kotlinx.coroutines.delay(1500)
        
        wifiDirectManager.createGroup()
    }

    override fun acceptConnection(sessionId: String) {
        pendingAuthorization?.complete(true)
        pendingAuthorization = null
    }

    override fun rejectConnection(sessionId: String) {
        pendingAuthorization?.complete(false)
        pendingAuthorization = null
        _currentTransfer.value = _currentTransfer.value?.copy(status = TransferStatus.CANCELLED)
    }

    private fun handleOutgoingTransfer(socket: Socket, files: List<TransferFile>) {
        repositoryScope.launch {
            val peer = socket.inetAddress.hostAddress ?: "Receiver"
            startService()
            android.util.Log.d("Transfer", "Client connected to TCP from $peer, starting auth.")
            
            try {
                socket.use { s ->
                    val out = java.io.DataOutputStream(s.getOutputStream())
                    val input = java.io.DataInputStream(s.getInputStream())

                    fun writeMsg(msg: TransferMessage) {
                        val bytes = TransferProtocol.encode(msg).toByteArray(Charsets.UTF_8)
                        out.writeInt(bytes.size)
                        out.write(bytes)
                        out.flush()
                    }
                    
                    fun readMsg(): TransferMessage {
                        val length = input.readInt()
                        val bytes = ByteArray(length)
                        input.readFully(bytes)
                        return TransferProtocol.decode(String(bytes, Charsets.UTF_8))
                    }
                    
                    val requestMsg = readMsg()
                    if (requestMsg !is TransferMessage.ConnectionRequest) throw Exception("Unexpected message")
                    
                    val peerName = requestMsg.deviceName
                    
                    val sessionId = UUID.randomUUID().toString()
                    val session = TransferSession(
                        sessionId = sessionId,
                        peerDevice = DeviceInfo(peerName, peer),
                        files = files,
                        status = TransferStatus.AWAITING_AUTHORIZATION,
                        isReceiver = false
                    )
                    _currentTransfer.value = session

                    val deferred = kotlinx.coroutines.CompletableDeferred<Boolean>()
                    pendingAuthorization = deferred
                    val accepted = deferred.await()
                    
                    writeMsg(TransferMessage.ConnectionResponse(accepted))
                    
                    if (!accepted) {
                        _currentTransfer.value = session.copy(status = TransferStatus.CANCELLED)
                        return@launch
                    }
                    
                    _currentTransfer.value = session.copy(status = TransferStatus.TRANSFERRING)

                    writeMsg(TransferMessage.Handshake(sessionId, wifiDirectManager.getLocalDeviceName()))
                    
                    val sessionTotalSize = session.totalSize
                    var totalSent = 0L
                    files.forEachIndexed { index, file ->
                        android.util.Log.d("Transfer", "Sending file: ${file.name}")
                        val metadata = TransferMessage.FileMetadata(file.name, file.size, file.mimeType, files.size, index)
                        writeMsg(metadata)
                        
                        transferEngine.sendFile(s, file.uri!!, file.name, file.size, sessionId, index, totalSent, sessionTotalSize)
                        totalSent += file.size
                    }
                    
                    writeMsg(TransferMessage.TransferComplete(sessionId))
                }
                _currentTransfer.value = _currentTransfer.value?.copy(status = TransferStatus.COMPLETED)
                selectionRepository.clearSelection()
                pendingFiles = null
            } catch (e: Exception) {
                android.util.Log.e("Transfer", "Transfer failed: ${e.message}")
                e.printStackTrace()
                val current = _currentTransfer.value
                if (current != null) {
                    _currentTransfer.value = current.copy(
                        status = TransferStatus.FAILED,
                        errorMessage = "Connection lost. Please ensure the receiver is nearby and try again."
                    )
                }
            }
        }
    }

    override suspend fun startReceiving() {
        _currentTransfer.value = null // Reset state for a fresh receiving session
        pendingFiles = null
        
        // Deep reset
        socketServer.stop()
        _transferProgress.value = null
        wifiDirectManager.disconnect() // Clear any existing legacy group/connection
        kotlinx.coroutines.delay(1500) // Essential delay for hardware state reset
    }

    override suspend fun connectToSenderDirectly(ip: String) {
        // Run on IO dispatcher to ensure non-blocking
        withContext(Dispatchers.IO) {
            val socket = socketClient.connect(ip, TRANSFER_PORT)
            if (socket != null) {
                pendingFiles = null
                processSocket(socket)
            } else {
                android.util.Log.e("Transfer", "Failed to explicitly connect TCP to $ip:$TRANSFER_PORT")
                _currentTransfer.value = TransferSession(
                    sessionId = "",
                    peerDevice = DeviceInfo("System", "Internal"),
                    files = emptyList(),
                    status = TransferStatus.FAILED,
                    isReceiver = true,
                    errorMessage = "Failed to connect to sender. They might be out of range."
                )
            }
        }
    }

    private fun handleIncomingTransfer(socket: Socket) {
        repositoryScope.launch {
            var sessionId = ""
            startService()
            
            try {
                socket.use { s ->
                    val out = java.io.DataOutputStream(s.getOutputStream())
                    val input = java.io.DataInputStream(s.getInputStream())

                    fun writeMsg(msg: TransferMessage) {
                        val bytes = TransferProtocol.encode(msg).toByteArray(Charsets.UTF_8)
                        out.writeInt(bytes.size)
                        out.write(bytes)
                        out.flush()
                    }
                    
                    fun readMsg(): TransferMessage {
                        val length = input.readInt()
                        val bytes = ByteArray(length)
                        input.readFully(bytes)
                        return TransferProtocol.decode(String(bytes, Charsets.UTF_8))
                    }
                    
                    // Send connection request
                    writeMsg(TransferMessage.ConnectionRequest(wifiDirectManager.getLocalDeviceName()))
                    
                    _currentTransfer.value = TransferSession(
                        sessionId = "",
                        peerDevice = DeviceInfo("Sender", socket.inetAddress.hostAddress ?: ""),
                        files = emptyList(),
                        status = TransferStatus.REQUESTED_CONNECTION,
                        isReceiver = true
                    )
                    
                    val responseMsg = readMsg()
                    if (responseMsg !is TransferMessage.ConnectionResponse) throw Exception("Unexpected response")
                    
                    if (!responseMsg.accepted) {
                        _currentTransfer.value = _currentTransfer.value?.copy(status = TransferStatus.CANCELLED)
                        return@launch
                    }
                    
                    _currentTransfer.value = _currentTransfer.value?.copy(status = TransferStatus.TRANSFERRING)

                    var totalReceived = 0L
                    while (true) {
                        val message = try { readMsg() } catch(e: java.io.EOFException) { break }
                        when (message) {
                            is TransferMessage.Handshake -> {
                                sessionId = message.sessionId
                                _currentTransfer.value = _currentTransfer.value?.copy(
                                    sessionId = sessionId,
                                    peerDevice = DeviceInfo(message.deviceName, socket.inetAddress.hostAddress ?: "")
                                )
                            }
                            is TransferMessage.FileMetadata -> {
                                val currentSession = _currentTransfer.value
                                if (currentSession != null) {
                                    // Check if we already have this file to avoid double-counting
                                    val alreadyExists = currentSession.files.size > message.index
                                    val updatedFiles = if (alreadyExists) {
                                        currentSession.files.toMutableList().apply {
                                            this[message.index] = TransferFile(message.name, message.size, message.mimeType, null)
                                        }
                                    } else {
                                        currentSession.files + TransferFile(message.name, message.size, message.mimeType, null)
                                    }
                                    _currentTransfer.value = currentSession.copy(files = updatedFiles)
                                }
                                
                                // Recalculate total size after update
                                val sessionTotalSize = _currentTransfer.value?.totalSize ?: 0L
                                transferEngine.receiveFile(s, message.name, message.size, sessionId, message.index, totalReceived, sessionTotalSize)
                                totalReceived += message.size
                            }
                            is TransferMessage.TransferComplete -> {
                                _currentTransfer.value = _currentTransfer.value?.copy(status = TransferStatus.COMPLETED)
                                break
                            }
                            else -> {}
                        }
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Transfer", "Incoming transfer failed: ${e.message}")
                e.printStackTrace()
                val current = _currentTransfer.value
                if (current != null) {
                    _currentTransfer.value = current.copy(
                        status = TransferStatus.FAILED,
                        errorMessage = "Connection lost. Please ensure the sender is nearby and try again."
                    )
                }
            }
        }
    }

    override suspend fun pauseTransfer(sessionId: String) {}

    override suspend fun resumeTransfer(sessionId: String) {}

    override suspend fun cancelTransfer(sessionId: String) {
        _currentTransfer.value = null
        socketServer.stop()
        wifiDirectManager.disconnect()
        pendingFiles = null
    }

    override fun clearTransferState() {
        _currentTransfer.value = null
        pendingFiles = null
    }
}
