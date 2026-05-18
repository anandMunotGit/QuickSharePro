package com.example.quicksharepro.ui.screens.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quicksharepro.domain.repository.FileSelectionRepository
import com.example.quicksharepro.domain.repository.FileTransferRepository
import com.example.quicksharepro.domain.repository.WifiDirectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SendViewModel @Inject constructor(
    private val transferRepository: FileTransferRepository,
    private val wifiRepository: WifiDirectRepository,
    private val selectionRepository: FileSelectionRepository
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

    val currentTransfer = transferRepository.currentTransfer

    fun startHotspot() {
        viewModelScope.launch {
            _isWaiting.value = true
            val files = selectionRepository.selectedFiles.value
            transferRepository.startSending(files)
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
