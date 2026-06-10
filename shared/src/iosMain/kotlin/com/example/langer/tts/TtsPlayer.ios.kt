package com.example.langer.tts

import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechBoundary

actual object TtsPlayer {
    private val synthesizer = AVSpeechSynthesizer()

    actual fun speak(text: String, language: String) {
        try {
            val utterance = AVSpeechUtterance.speechUtteranceWithString(text)
            val voiceLanguage = if (language == "en") "en-US" else language
            utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage(voiceLanguage)
            
            if (synthesizer.isSpeaking()) {
                synthesizer.stopSpeakingAtBoundary(AVSpeechBoundary.AVSpeechBoundaryImmediate)
            }
            synthesizer.speakUtterance(utterance)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
