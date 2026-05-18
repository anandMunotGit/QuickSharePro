package com.example.quicksharepro.domain.usecase

import com.example.quicksharepro.domain.model.TransferFile
import com.example.quicksharepro.domain.repository.FileTransferRepository
import javax.inject.Inject

class SendFilesUseCase @Inject constructor(
    private val repository: FileTransferRepository
) {
    suspend operator fun invoke(files: List<TransferFile>) {
        repository.startSending(files)
    }
}
