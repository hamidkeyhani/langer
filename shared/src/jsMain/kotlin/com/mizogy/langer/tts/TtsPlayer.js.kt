package com.mizogy.langer.tts

import kotlinx.browser.window

actual object TtsPlayer {
    actual fun speak(text: String, language: String, enqueue: Boolean) {
        try {
            val synth = window.asDynamic().speechSynthesis
            if (synth != null) {
                if (!enqueue) {
                    synth.cancel() // Stop any current speech
                }
                val utterance = js("new SpeechSynthesisUtterance(text)")
                utterance.lang = if (language == "en") "en-US" else language
                synth.speak(utterance)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
