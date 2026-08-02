package com.mizogy.langer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform