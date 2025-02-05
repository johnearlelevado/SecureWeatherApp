package com.example.secureweatherapp.security

import android.content.Context
import android.provider.Settings

object DeviceSecurity {
    fun isDeveloperOptionsEnabled(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0
        ) != 0
    }
}