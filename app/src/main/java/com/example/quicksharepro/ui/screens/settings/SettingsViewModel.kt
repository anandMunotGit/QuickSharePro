package com.example.quicksharepro.ui.screens.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _deviceName = MutableStateFlow("Android Device")
    val deviceName = _deviceName.asStateFlow()

    private val _saveToGallery = MutableStateFlow(true)
    val saveToGallery = _saveToGallery.asStateFlow()

    fun updateDeviceName(name: String) {
        _deviceName.value = name
    }

    fun toggleSaveToGallery(enabled: Boolean) {
        _saveToGallery.value = enabled
    }
}
