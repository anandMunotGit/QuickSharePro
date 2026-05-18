package com.example.quicksharepro.domain.usecase

import com.example.quicksharepro.domain.repository.WifiDirectRepository
import javax.inject.Inject

class DiscoverDevicesUseCase @Inject constructor(
    private val repository: WifiDirectRepository
) {
    operator fun invoke() = repository.peers

    fun start() = repository.startDiscovery()
    fun stop() = repository.stopDiscovery()
}
