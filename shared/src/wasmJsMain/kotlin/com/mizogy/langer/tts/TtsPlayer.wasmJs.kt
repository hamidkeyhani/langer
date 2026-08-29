package com.mizogy.langer.tts

@JsFun("(text, lang, enqueue) => { if (typeof window !== 'undefined' && window.speechSynthesis) { if (!enqueue) { window.speechSynthesis.cancel(); } const u = new SpeechSynthesisUtterance(text); u.lang = lang; window.speechSynthesis.speak(u); } }")
private external fun jsSpeak(text: String, lang: String, enqueue: Boolean)

@JsFun("() => { if (typeof window !== 'undefined' && window.speechSynthesis) { window.speechSynthesis.cancel(); } }")
private external fun jsStop()

actual object TtsPlayer {
    actual fun speak(text: String, language: String, enqueue: Boolean) {
        try {
            val voiceLanguage = if (language == "en") "en-US" else language
            jsSpeak(text, voiceLanguage, enqueue)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual fun stop() {
        try {
            jsStop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
