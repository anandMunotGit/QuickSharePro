package com.example.quicksharepro.ui.screens.receive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quicksharepro.domain.repository.FileTransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiveViewModel @Inject constructor(
    private val transferRepository: FileTransferRepository,
    private val wifiRepository: com.example.quicksharepro.domain.repository.WifiDirectRepository
) : ViewModel() {
    
    private val _isWaiting = MutableStateFlow(false)
    val isWaiting = _isWaiting.asStateFlow()

    val isWifiEnabled = wifiRepository.isWifiP2pEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isLocationEnabled = wifiRepository.isLocationEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val qrCodeData = wifiRepository.qrCodeData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun requestWifiEnabled() {
        wifiRepository.requestWifiEnabled()
    }

    fun requestLocationEnabled() {
        wifiRepository.requestLocationEnabled()
    }

    val currentTransfer = transferRepository.currentTransfer

    fun startListening() {
        viewModelScope.launch {
            _isWaiting.value = true
            _connectionError.value = null // Clear error to resume scanner listener in UI
            transferRepository.startReceiving()
        }
    }

    private val _isConnecting = MutableStateFlow(false)
    val isConnecting = _isConnecting.asStateFlow()

    private val _connectionError = MutableStateFlow<String?>(null)
    val connectionError = _connectionError.asStateFlow()

    private val _isScannerEnabled = MutableStateFlow(true)
    val isScannerEnabled = _isScannerEnabled.asStateFlow()

    fun dismissError() {
        _connectionError.value = null
        _isScannerEnabled.value = true
    }

    fun connectFromQR(qrData: String, onConnected: () -> Unit) {
        val ssidMatch = Regex("S:([^;]+);").find(qrData)
        val passMatch = Regex("P:([^;]+);").find(qrData)
        
        val ssid = ssidMatch?.groups?.get(1)?.value
        val pass = passMatch?.groups?.get(1)?.value
        
        if (ssid != null && pass != null) {
            _isConnecting.value = true
            _connectionError.value = null
            
            // Set a timeout for the Wi-Fi connection attempt (reverted to 15s for speed)
            val connectionJob = viewModelScope.launch {
                kotlinx.coroutines.delay(15000) 
                if (_isConnecting.value) {
                    _isConnecting.value = false
                    _isScannerEnabled.value = false
                    _connectionError.value = "Connection timed out. The Sender might not be ready yet."
                }
            }

            wifiRepository.connectToNetwork(ssid, pass, 
                onConnected = {
                    connectionJob.cancel()
                    viewModelScope.launch {
                        transferRepository.connectToSenderDirectly("192.168.49.1")
                        _isConnecting.value = false
                        onConnected()
                    }
                },
                onError = {
                    connectionJob.cancel()
                    _isConnecting.value = false
                    _isScannerEnabled.value = false
                    _connectionError.value = "Failed to join network. Please ensure the QR is still valid."
                }
            )
        } else {
            _connectionError.value = "Invalid QR Code format."
        }
    }

    fun acceptConnection() {
        currentTransfer.value?.sessionId?.let {
            transferRepository.acceptConnection(it)
        }
    }

    fun rejectConnection() {
        currentTransfer.value?.sessionId?.let {
            transferRepository.rejectConnection(it)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            transferRepository.cancelTransfer("")
        }
    }
}
