package com.example.quicksharepro.domain.model

data class DeviceInfo(
    val deviceName: String,
    val deviceAddress: String,
    val isHeader: Boolean = false,
    val status: String = ""
)
