package com.example.langer.storage

import kotlinx.browser.window

class WasmStorage : KeyValueStorage {
    override fun getString(key: String): String? {
        return window.localStorage.getItem(key)
    }

    override fun putString(key: String, value: String) {
        window.localStorage.setItem(key, value)
    }
}

actual fun getPlatformStorage(): KeyValueStorage {
    return WasmStorage()
}
