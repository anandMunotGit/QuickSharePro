package com.example.quicksharepro.ui.screens.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quicksharepro.domain.model.TransferProgress
import com.example.quicksharepro.domain.model.TransferSession
import com.example.quicksharepro.domain.repository.FileTransferRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val repository: FileTransferRepository
) : ViewModel() {
    
    val currentTransfer = repository.currentTransfer.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val progress = repository.transferProgress.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun cancelTransfer(sessionId: String) {
        viewModelScope.launch {
            repository.cancelTransfer(sessionId)
        }
    }

    fun clearState() {
        repository.clearTransferState()
    }
}
