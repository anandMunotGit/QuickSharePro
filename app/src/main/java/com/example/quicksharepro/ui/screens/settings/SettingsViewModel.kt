package com.example.quicksharepro.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.example.quicksharepro.data.local.SettingsPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsPreferences
) : ViewModel() {
    private val _deviceName = MutableStateFlow(settings.deviceName)
    val deviceName = _deviceName.asStateFlow()

    private val _saveToGallery = MutableStateFlow(settings.saveToGallery)
    val saveToGallery = _saveToGallery.asStateFlow()

    fun updateDeviceName(name: String) {
        _deviceName.value = name
        settings.deviceName = name
    }

    fun toggleSaveToGallery(enabled: Boolean) {
        _saveToGallery.value = enabled
        settings.saveToGallery = enabled
    }
}
