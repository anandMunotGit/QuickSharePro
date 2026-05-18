package com.example.quicksharepro.data.transfer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocketClient @Inject constructor() {
    suspend fun connect(
        address: String, 
        port: Int, 
        maxRetries: Int = 50, 
        retryDelayMs: Long = 300
    ): Socket? = withContext(Dispatchers.IO) {
        for (i in 1..maxRetries) {
            try {
                android.util.Log.d("SocketClient", "Attempt $i to connect to $address:$port")
                val socket = Socket()
                socket.connect(InetSocketAddress(address, port), 1000)
                android.util.Log.d("SocketClient", "Connected successfully on attempt $i")
                return@withContext socket
            } catch (e: Exception) {
                android.util.Log.e("SocketClient", "Failed on attempt $i: ${e.message}")
                if (i < maxRetries) {
                    kotlinx.coroutines.delay(retryDelayMs)
                }
            }
        }
        return@withContext null
    }
}
