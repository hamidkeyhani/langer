package com.example.langer.tts

expect object TtsPlayer {
    fun speak(text: String, language: String = "en")
}
