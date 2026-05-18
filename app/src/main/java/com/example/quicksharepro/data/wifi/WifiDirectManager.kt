package com.example.quicksharepro.data.wifi

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import android.os.Build
import android.provider.Settings
import com.example.quicksharepro.data.local.SettingsPreferences
import com.example.quicksharepro.domain.model.DeviceInfo
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiDirectManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsPreferences
) {
    private val manager: WifiP2pManager? = context.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private val channel: WifiP2pManager.Channel? = manager?.initialize(context, context.mainLooper, null)

    private val _peers = MutableStateFlow<List<DeviceInfo>>(emptyList())
    val peers = _peers.asStateFlow()

    private val _connectionInfo = MutableStateFlow<String>("Disconnected")
    val connectionInfo = _connectionInfo.asStateFlow()

    private val _isWifiP2pEnabled = MutableStateFlow(false)
    val isWifiP2pEnabled = _isWifiP2pEnabled.asStateFlow()

    private val _groupOwnerAddress = MutableStateFlow<String?>(null)
    val groupOwnerAddress = _groupOwnerAddress.asStateFlow()

    private val _isGroupOwner = MutableStateFlow(false)
    val isGroupOwner = _isGroupOwner.asStateFlow()

    private val _isLocationEnabled = MutableStateFlow(false)
    val isLocationEnabled = _isLocationEnabled.asStateFlow()

    private val _thisDevice = MutableStateFlow<DeviceInfo?>(null)
    val thisDevice = _thisDevice.asStateFlow()

    private val _qrCodeData = MutableStateFlow<String?>(null)
    val qrCodeData = _qrCodeData.asStateFlow()

    private var receiver: WifiDirectBroadcastReceiver? = null
    private var locationReceiver: android.content.BroadcastReceiver? = null
    private var currentNetworkCallback: android.net.ConnectivityManager.NetworkCallback? = null

    init {
        checkHardware()
        registerReceiver()
    }

    fun checkHardware() {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? android.location.LocationManager
        val isLocationOn = locationManager?.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) == true

        _isLocationEnabled.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            true // Location is not strictly required for nearby WiFi discovery on Android 13+
        } else {
            isLocationOn
        }
        
        @Suppress("DEPRECATION")
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager
        _isWifiP2pEnabled.value = wifiManager?.isWifiEnabled == true
    }

    private fun registerReceiver() {
        receiver = WifiDirectBroadcastReceiver(
            manager, channel,
            onStateChanged = { enabled -> _isWifiP2pEnabled.value = enabled },
            onPeersChanged = { peerList ->
                val devices = peerList.deviceList.map { device ->
                    DeviceInfo(device.deviceName, device.deviceAddress, status = getStatusString(device.status))
                }
                _peers.value = devices
            },
            onConnectionChanged = { info ->
                _isGroupOwner.value = info.isGroupOwner
                _groupOwnerAddress.value = info.groupOwnerAddress?.hostAddress
                
                if (info.groupFormed) {
                    if (info.isGroupOwner) {
                        _connectionInfo.value = "Group Owner (Server)"
                        // The group is finally verified as formed by the Android OS! Extract the credentials securely.
                        requestGroupInfo { group ->
                            val ssid = group?.networkName
                            val pass = group?.passphrase
                            if (ssid != null && pass != null) {
                                _qrCodeData.value = "WIFI:T:WPA;S:$ssid;P:$pass;;"
                                android.util.Log.d("P2P", "Generated Secure QR for network $ssid")
                            }
                        }
                    } else {
                        _connectionInfo.value = "Group Client"
                    }
                } else {
                    _connectionInfo.value = "Disconnected"
                }
            },
            onThisDeviceChanged = { device ->
                _thisDevice.value = DeviceInfo(device.deviceName, device.deviceAddress)
            }
        )
        val intentFilter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        }
        context.registerReceiver(receiver, intentFilter)

        locationReceiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                if (intent.action == android.location.LocationManager.PROVIDERS_CHANGED_ACTION) {
                    checkHardware()
                }
            }
        }
        context.registerReceiver(
            locationReceiver,
            IntentFilter(android.location.LocationManager.PROVIDERS_CHANGED_ACTION)
        )
    }

    @SuppressLint("MissingPermission")
    fun startDiscovery() {
        android.util.Log.d("P2P", "Starting discovery...")
        manager?.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                android.util.Log.d("P2P", "Discovery initiated successfully")
            }
            override fun onFailure(reason: Int) {
                android.util.Log.e("P2P", "Discovery failed: $reason")
                if (reason == WifiP2pManager.BUSY) {
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        startDiscovery()
                    }, 1000)
                }
            }
        })
    }

    fun stopDiscovery() {
        manager?.stopPeerDiscovery(channel, null)
    }

    @SuppressLint("MissingPermission")
    fun connect(device: DeviceInfo) {
        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
        }
        manager?.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                android.util.Log.d("P2P", "Connect physically requested to ${device.deviceAddress}")
            }
            override fun onFailure(reason: Int) {
                android.util.Log.e("P2P", "Native connection failed! Reason code: $reason")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun createGroup() {
        android.util.Log.d("P2P", "Attempting to create group...")
        manager?.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                android.util.Log.d("P2P", "Group creation initiated. Waiting for OS broadcast...")
            }
            override fun onFailure(reason: Int) {
                android.util.Log.e("P2P", "Failed to initiate group. Reason code: $reason")
            }
        })
    }

    fun getLocalDeviceName(): String {
        return settings.deviceName
    }

    fun disconnect() {
        manager?.cancelConnect(channel, null)
        manager?.removeGroup(channel, null)
        
        // Clear network callbacks and bindings
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        currentNetworkCallback?.let {
            try {
                connectivityManager.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        currentNetworkCallback = null
        connectivityManager.bindProcessToNetwork(null)
        
        _qrCodeData.value = null
        _connectionInfo.value = "Disconnected"
    }

    fun requestWifiEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Settings.Panel.ACTION_WIFI)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager)?.isWifiEnabled = true
        }
    }

    fun requestLocationEnabled() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun requestGroupInfo(onInfoReceived: (android.net.wifi.p2p.WifiP2pGroup?) -> Unit) {
        @SuppressLint("MissingPermission")
        manager?.requestGroupInfo(channel) { group ->
            onInfoReceived(group)
        }
    }

    fun connectToNetwork(ssid: String, password: String, onConnected: () -> Unit, onError: () -> Unit = {}) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            
            // Unregister any previous callback before starting a new one
            currentNetworkCallback?.let {
                try {
                    connectivityManager.unregisterNetworkCallback(it)
                } catch (e: Exception) {}
            }

            val specifier = android.net.wifi.WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .setWpa2Passphrase(password)
                .build()

            val request = android.net.NetworkRequest.Builder()
                .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build()

            val networkCallback = object : android.net.ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    super.onAvailable(network)
                    connectivityManager.bindProcessToNetwork(network)
                    onConnected()
                }
                override fun onUnavailable() {
                    super.onUnavailable()
                    connectivityManager.bindProcessToNetwork(null)
                    currentNetworkCallback = null
                    onError()
                }
                override fun onLost(network: android.net.Network) {
                    super.onLost(network)
                    connectivityManager.bindProcessToNetwork(null)
                    currentNetworkCallback = null
                    onError()
                }
            }
            
            currentNetworkCallback = networkCallback
            connectivityManager.requestNetwork(request, networkCallback)
        }
    }

    private fun getStatusString(status: Int): String {
        return when (status) {
            WifiP2pDevice.CONNECTED -> "Connected"
            WifiP2pDevice.INVITED -> "Invited"
            WifiP2pDevice.FAILED -> "Failed"
            WifiP2pDevice.AVAILABLE -> "Available"
            WifiP2pDevice.UNAVAILABLE -> "Unavailable"
            else -> "Unknown"
        }
    }
}
