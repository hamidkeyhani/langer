package com.example.langer.tts

import android.speech.tts.TextToSpeech
import com.example.langer.storage.appContext
import java.util.Locale

actual object TtsPlayer {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private fun initTts(onReady: () -> Unit) {
        if (isInitialized) {
            onReady()
            return
        }
        tts = TextToSpeech(appContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                onReady()
            }
        }
    }

    actual fun speak(text: String, language: String) {
        initTts {
            tts?.let { player ->
                player.language = if (language == "en") Locale.US else Locale(language)
                player.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            }
        }
    }
}
