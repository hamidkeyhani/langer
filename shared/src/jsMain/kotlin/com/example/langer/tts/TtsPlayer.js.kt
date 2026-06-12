package com.example.langer.tts

import kotlinx.browser.window

actual object TtsPlayer {
    actual fun speak(text: String, language: String) {
        try {
            val synth = window.asDynamic().speechSynthesis
            if (synth != null) {
                synth.cancel() // Stop any current speech
                val utterance = js("new SpeechSynthesisUtterance(text)")
                utterance.lang = if (language == "en") "en-US" else language
                synth.speak(utterance)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
