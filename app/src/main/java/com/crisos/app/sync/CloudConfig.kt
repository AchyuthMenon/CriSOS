package com.crisos.app.sync

import android.content.Context
import com.crisos.app.BuildConfig

/**
 * Manages configurable remote backend API URL for MongoDB Atlas cloud synchronization.
 */
object CloudConfig {
    private const val PREFS_NAME = "crisos_cloud_prefs"
    private const val KEY_BACKEND_URL = "backend_api_url"

    /**
     * Retrieve the active backend API base URL (e.g. "https://my-backend.com" or "http://10.0.2.2:3000").
     */
    fun getBackendUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_BACKEND_URL, null)
        return if (!saved.isNullOrBlank()) {
            saved.trim().trimEnd('/')
        } else {
            BuildConfig.DEFAULT_BACKEND_URL.trimEnd('/')
        }
    }

    /**
     * Update the backend API base URL.
     */
    fun setBackendUrl(context: Context, url: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BACKEND_URL, url.trim().trimEnd('/')).apply()
    }
}
