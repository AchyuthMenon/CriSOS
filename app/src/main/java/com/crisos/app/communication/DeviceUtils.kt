package com.crisos.app.communication

import android.content.Context
import android.provider.Settings

object DeviceUtils {
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: android.os.Build.MODEL
            ?: "unknown_device"
    }
}
