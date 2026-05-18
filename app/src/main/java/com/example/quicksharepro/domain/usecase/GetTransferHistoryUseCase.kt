package com.example.quicksharepro.domain.usecase

import com.example.quicksharepro.domain.repository.TransferHistoryRepository
import javax.inject.Inject

class GetTransferHistoryUseCase @Inject constructor(
    private val repository: TransferHistoryRepository
) {
    operator fun invoke() = repository.getHistory()
}
