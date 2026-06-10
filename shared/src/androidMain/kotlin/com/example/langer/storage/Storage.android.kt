package com.example.langer.storage

import android.content.Context

lateinit var appContext: Context

class AndroidStorage(private val context: Context) : KeyValueStorage {
    private val prefs = context.getSharedPreferences("langer_prefs", Context.MODE_PRIVATE)

    override fun getString(key: String): String? {
        return prefs.getString(key, null)
    }

    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}

actual fun getPlatformStorage(): KeyValueStorage {
    return AndroidStorage(appContext)
}
