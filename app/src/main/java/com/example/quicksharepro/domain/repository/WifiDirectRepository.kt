package com.example.quicksharepro.domain.repository

import com.example.quicksharepro.domain.model.DeviceInfo
import kotlinx.coroutines.flow.Flow

interface WifiDirectRepository {
    val peers: Flow<List<DeviceInfo>>
    val connectionStatus: Flow<String>
    val isWifiP2pEnabled: Flow<Boolean>
    val isLocationEnabled: Flow<Boolean>
    val isGroupOwner: Flow<Boolean>
    val qrCodeData: Flow<String?>

    fun startDiscovery()
    fun stopDiscovery()
    fun createGroup()
    fun checkHardware()
    fun connect(device: DeviceInfo)
    fun connectToNetwork(ssid: String, pass: String, onConnected: () -> Unit, onError: () -> Unit = {})
    fun disconnect()
    fun getLocalDeviceName(): String
    fun requestWifiEnabled()
    fun requestLocationEnabled()
}
