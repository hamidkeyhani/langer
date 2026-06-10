package com.example.langer.storage

import java.io.File
import java.util.Properties

class JvmStorage : KeyValueStorage {
    private val dataDir = File(System.getProperty("user.home"), ".langer")
    private val dataFile = File(dataDir, "storage.properties")
    private val properties = Properties()

    init {
        try {
            if (!dataDir.exists()) {
                dataDir.mkdirs()
            }
            if (dataFile.exists()) {
                dataFile.inputStream().use { properties.load(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getString(key: String): String? {
        synchronized(properties) {
            return properties.getProperty(key)
        }
    }

    override fun putString(key: String, value: String) {
        synchronized(properties) {
            properties.setProperty(key, value)
            try {
                dataFile.outputStream().use { properties.store(it, "Langer App Storage") }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

actual fun getPlatformStorage(): KeyValueStorage {
    return JvmStorage()
}
