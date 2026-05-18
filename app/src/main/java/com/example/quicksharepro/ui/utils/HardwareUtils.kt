package com.example.quicksharepro.ui.utils

import android.app.Activity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

object HardwareUtils {
    fun promptEnableLocation(activity: Activity) {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_LOW_POWER, 10000).build()
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
            .setAlwaysShow(true)

        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // This shows the system dialog within the app
                    exception.startResolutionForResult(activity, 9001)
                } catch (sendEx: Exception) {
                    // Ignore or log
                }
            }
        }
    }
}
