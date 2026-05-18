package com.example.quicksharepro.data.transfer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.ServerSocket
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketServer @Inject constructor() {
    private var serverSocket: ServerSocket? = null
    
    suspend fun startListening(port: Int, onClientConnected: suspend (Socket) -> Unit) = withContext(Dispatchers.IO) {
        try {
            stop() // Ensure previous socket is closed
            serverSocket = ServerSocket(port).apply {
                reuseAddress = true
            }
            while (true) {
                val socket = serverSocket?.accept() ?: break
                onClientConnected(socket)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stop()
        }
    }

    fun stop() {
        serverSocket?.close()
        serverSocket = null
    }
}
