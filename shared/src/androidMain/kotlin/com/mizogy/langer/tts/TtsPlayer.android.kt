package com.mizogy.langer.tts

import android.speech.tts.TextToSpeech
import com.mizogy.langer.storage.appContext
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

    actual fun speak(text: String, language: String, enqueue: Boolean) {
        initTts {
            tts?.let { player ->
                player.language = if (language == "en") Locale.US else Locale(language)
                val mode = if (enqueue) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH
                player.speak(text, mode, null, null)
            }
        }
    }
}
