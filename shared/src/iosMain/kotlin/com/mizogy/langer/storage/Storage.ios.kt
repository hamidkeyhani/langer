package com.mizogy.langer.storage

import platform.Foundation.NSUserDefaults

class IosStorage : KeyValueStorage {
    override fun getString(key: String): String? {
        return NSUserDefaults.standardUserDefaults.stringForKey(key)
    }

    override fun putString(key: String, value: String) {
        NSUserDefaults.standardUserDefaults.setObject(value, key)
        NSUserDefaults.standardUserDefaults.synchronize()
    }
}

actual fun getPlatformStorage(): KeyValueStorage {
    return IosStorage()
}
