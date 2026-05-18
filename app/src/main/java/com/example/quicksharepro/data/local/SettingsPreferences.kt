package com.example.quicksharepro.data.local

import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs = context.getSharedPreferences("quickshare_settings", Context.MODE_PRIVATE)

    var deviceName: String
        get() = prefs.getString("device_name", Build.MODEL) ?: Build.MODEL
        set(value) = prefs.edit().putString("device_name", value).apply()

    var saveToGallery: Boolean
        get() = prefs.getBoolean("save_to_gallery", true)
        set(value) = prefs.edit().putBoolean("save_to_gallery", value).apply()
}
