package com.example.quicksharepro.data.repository

import com.example.quicksharepro.data.wifi.WifiDirectManager
import com.example.quicksharepro.domain.model.DeviceInfo
import com.example.quicksharepro.domain.repository.WifiDirectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiDirectRepositoryImpl @Inject constructor(
    private val wifiDirectManager: WifiDirectManager
) : WifiDirectRepository {
    
    override val peers: Flow<List<DeviceInfo>> = wifiDirectManager.peers
    override val connectionStatus: Flow<String> = wifiDirectManager.connectionInfo
    override val isWifiP2pEnabled: Flow<Boolean> = wifiDirectManager.isWifiP2pEnabled
    override val isLocationEnabled: Flow<Boolean> = wifiDirectManager.isLocationEnabled
    override val isGroupOwner: Flow<Boolean> = wifiDirectManager.isGroupOwner
    override val qrCodeData: Flow<String?> = wifiDirectManager.qrCodeData

    override fun startDiscovery() = wifiDirectManager.startDiscovery()
    
    override fun stopDiscovery() = wifiDirectManager.stopDiscovery()

    override fun checkHardware() = wifiDirectManager.checkHardware()

    override fun createGroup() = wifiDirectManager.createGroup()
    
    override fun connect(device: DeviceInfo) = wifiDirectManager.connect(device)
    
    override fun connectToNetwork(ssid: String, pass: String, onConnected: () -> Unit, onError: () -> Unit) {
        wifiDirectManager.connectToNetwork(ssid, pass, onConnected, onError)
    }
    
    override fun disconnect() = wifiDirectManager.disconnect()

    override fun getLocalDeviceName(): String = wifiDirectManager.thisDevice.value?.deviceName ?: "Android Device"

    override fun requestWifiEnabled() = wifiDirectManager.requestWifiEnabled()

    override fun requestLocationEnabled() = wifiDirectManager.requestLocationEnabled()
}
