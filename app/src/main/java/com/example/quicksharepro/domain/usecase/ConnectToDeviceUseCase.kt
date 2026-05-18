package com.example.quicksharepro.domain.usecase

import com.example.quicksharepro.domain.model.DeviceInfo
import com.example.quicksharepro.domain.repository.WifiDirectRepository
import javax.inject.Inject

class ConnectToDeviceUseCase @Inject constructor(
    private val repository: WifiDirectRepository
) {
    operator fun invoke(device: DeviceInfo) = repository.connect(device)
}
