package com.example.quicksharepro.domain.usecase

import com.example.quicksharepro.domain.repository.FileTransferRepository
import javax.inject.Inject

class ReceiveFilesUseCase @Inject constructor(
    private val repository: FileTransferRepository
) {
    suspend operator fun invoke() {
        repository.startReceiving()
    }
}
