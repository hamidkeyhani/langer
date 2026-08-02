package com.mizogy.langer.util

actual fun currentTimeMillis(): Long = kotlin.js.Date.now().toLong()
