package com.example.langer

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform