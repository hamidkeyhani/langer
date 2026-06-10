package com.example.langer.tts

import java.io.IOException

actual object TtsPlayer {
    actual fun speak(text: String, language: String) {
        val os = System.getProperty("os.name").lowercase()
        Thread {
            try {
                // Sanitize text for shell execution
                val sanitizedText = text.replace("'", "").replace("\"", "")
                when {
                    os.contains("mac") -> {
                        // macOS native 'say' command with system voices
                        val cmd = arrayOf("say", sanitizedText)
                        Runtime.getRuntime().exec(cmd)
                    }
                    os.contains("win") -> {
                        // Windows PowerShell speech synthesizer
                        val psCommand = "Add-Type -AssemblyName System.Speech; \$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer; \$synth.Speak('$sanitizedText')"
                        val cmd = arrayOf("powershell", "-Command", psCommand)
                        Runtime.getRuntime().exec(cmd)
                    }
                    else -> {
                        // Linux fallback
                        try {
                            Runtime.getRuntime().exec(arrayOf("spd-say", sanitizedText))
                        } catch (e: Exception) {
                            try {
                                Runtime.getRuntime().exec(arrayOf("espeak", sanitizedText))
                            } catch (ex: Exception) {
                                // Fallback failed, print stack trace
                                ex.printStackTrace()
                            }
                        }
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }.start()
    }
}
