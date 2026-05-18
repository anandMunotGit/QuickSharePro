package com.example.quicksharepro.ui.screens.home

import androidx.lifecycle.ViewModel
import com.example.quicksharepro.domain.repository.FileTransferRepository
import com.example.quicksharepro.domain.repository.WifiDirectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val wifiRepository: WifiDirectRepository,
    private val transferRepository: FileTransferRepository
) : ViewModel() {
    val wifiStatus = wifiRepository.isWifiP2pEnabled.map { if (it) "Ready" else "Wi-Fi Direct Disabled" }
    
    init {
        // Ensure transfer state is cleared when returning to dashboard
        transferRepository.clearTransferState()
    }

    fun getDeviceName() = wifiRepository.getLocalDeviceName()
}
