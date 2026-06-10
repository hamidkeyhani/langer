package com.example.langer.tts

@JsFun("(text, lang) => { if (typeof window !== 'undefined' && window.speechSynthesis) { window.speechSynthesis.cancel(); const u = new SpeechSynthesisUtterance(text); u.lang = lang; window.speechSynthesis.speak(u); } }")
private external fun jsSpeak(text: String, lang: String)

actual object TtsPlayer {
    actual fun speak(text: String, language: String) {
        try {
            val voiceLanguage = if (language == "en") "en-US" else language
            jsSpeak(text, voiceLanguage)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
