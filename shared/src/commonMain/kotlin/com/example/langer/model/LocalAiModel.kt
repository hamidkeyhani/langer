package com.example.langer.model

import kotlinx.serialization.json.Json
import langer.shared.generated.resources.Res

object LocalAiModel {
    private val jsonParser = Json { ignoreUnknownKeys = true }
    private var cachedSeedWords: List<SeedWord>? = null

    // Load words list from compose resources
    suspend fun getSeedWords(): List<SeedWord> {
        if (cachedSeedWords != null) return cachedSeedWords!!
        return try {
            val jsonText = Res.readBytes("files/essential_words.json").decodeToString()
            val words = jsonParser.decodeFromString<List<SeedWord>>(jsonText)
            cachedSeedWords = words
            words
        } catch (e: Exception) {
            emptyList()
        }
    }

    // AI generation logic
    suspend fun generateWordDetails(inputWord: String): SeedWord {
        val normalized = inputWord.trim().lowercase()
        if (normalized.isBlank()) return SeedWord(word = "", meaning = "")

        // 1. Check local seed words list
        val localWords = getSeedWords()
        val matched = localWords.find { it.word.trim().lowercase() == normalized }
        if (matched != null) {
            return matched
        }

        // 2. Check our extra dictionary of common words
        val extraMatched = extraDictionary[normalized]
        if (extraMatched != null) {
            return SeedWord(
                word = inputWord,
                phonetic = extraMatched.phonetic,
                meaning = extraMatched.meaning,
                example = extraMatched.example
            )
        }

        // 3. Rule-based AI generator for phonetic spelling
        val phonetic = generatePhonetic(normalized)

        // 4. Rule-based AI generator for meaning and example sentence
        val (meaning, example) = generateMeaningAndExample(inputWord)

        return SeedWord(
            word = inputWord,
            phonetic = phonetic,
            meaning = meaning,
            example = example
        )
    }

    private fun generatePhonetic(word: String): String {
        var result = word.lowercase().trim()
        
        // Apply digraph replacements
        result = result.replace("sh", "ʃ")
        result = result.replace("ch", "tʃ")
        result = result.replace("th", "θ")
        result = result.replace("ph", "f")
        result = result.replace("ng", "ŋ")
        result = result.replace("ee", "iː")
        result = result.replace("oo", "uː")
        result = result.replace("ou", "aʊ")
        result = result.replace("oi", "ɔɪ")
        result = result.replace("ai", "eɪ")
        result = result.replace("ay", "eɪ")
        result = result.replace("oy", "ɔɪ")
        
        // Single character mappings
        val sb = StringBuilder()
        for (char in result) {
            val mapped = when (char) {
                'a' -> 'æ'
                'e' -> 'e'
                'i' -> 'ɪ'
                'o' -> 'ɒ'
                'u' -> 'ʌ'
                'c' -> 'k'
                'q' -> 'k'
                'x' -> 'z'
                else -> char
            }
            sb.append(mapped)
        }
        return "/${sb.toString()}/"
    }

    private fun generateMeaningAndExample(word: String): Pair<String, String> {
        val w = word.trim().lowercase()

        return when {
            w.endsWith("tion") -> {
                val base = word.substringBeforeLast("tion")
                Pair(
                    "The action, process, or state of being ${base}ed.",
                    "The ${w} of the project took longer than expected."
                )
            }
            w.endsWith("ly") -> {
                val base = word.substringBeforeLast("ly")
                Pair(
                    "In a manner that is characteristic of being ${base}.",
                    "She performed the task extremely ${w} and efficiently."
                )
            }
            w.endsWith("able") || w.endsWith("ible") -> {
                val base = if (w.endsWith("able")) word.substringBeforeLast("able") else word.substringBeforeLast("ible")
                Pair(
                    "Capable of being, or worthy of being, ${base}ed.",
                    "The results were highly ${w} under the given conditions."
                )
            }
            w.endsWith("less") -> {
                val base = word.substringBeforeLast("less")
                Pair(
                    "Without or lacking any ${base}.",
                    "He felt completely ${w} in the face of the challenge."
                )
            }
            w.endsWith("ness") -> {
                val base = word.substringBeforeLast("ness")
                Pair(
                    "The state, quality, or condition of being ${base}.",
                    "Her ${w} was appreciated by everyone in the room."
                )
            }
            w.endsWith("ify") -> {
                val base = word.substringBeforeLast("ify")
                Pair(
                    "To make or cause to become ${base}.",
                    "They had to ${w} the credentials before proceeding."
                )
            }
            w.endsWith("ize") || w.endsWith("ise") -> {
                val base = if (w.endsWith("ize")) word.substringBeforeLast("ize") else word.substringBeforeLast("ise")
                Pair(
                    "To make, adapt, or convert into ${base}.",
                    "We need to ${w} the workflow to increase productivity."
                )
            }
            w.endsWith("er") || w.endsWith("or") -> {
                val base = if (w.endsWith("er")) word.substringBeforeLast("er") else word.substringBeforeLast("or")
                Pair(
                    "A person or thing that performs the action of ${base}ing.",
                    "As a professional ${w}, she has worked on many complex systems."
                )
            }
            w.endsWith("ful") -> {
                val base = word.substringBeforeLast("ful")
                Pair(
                    "Full of or characterized by ${base}.",
                    "It was a ${w} day filled with joy and celebration."
                )
            }
            w.endsWith("ment") -> {
                val base = word.substringBeforeLast("ment")
                Pair(
                    "The action, process, or state of ${base}ing, or the result thereof.",
                    "They reached an ${w} after hours of negotiation."
                )
            }
            else -> {
                Pair(
                    "To perform or relate to the action, quality, or concept of '${word}'.",
                    "We need to ${w} the new features during our next session."
                )
            }
        }
    }

    private val extraDictionary = mapOf(
        "learn" to SeedWord("learn", "/lɜːn/", "To acquire knowledge or skill in something by study or experience", "She wants to learn a new language."),
        "study" to SeedWord("study", "/ˈstʌdi/", "To devote time and attention to acquiring knowledge on an academic subject", "He spends hours in the library to study."),
        "code" to SeedWord("code", "/kəʊd/", "To write instructions for a computer program", "I love to code in Kotlin."),
        "langer" to SeedWord("langer", "/ˈlæŋər/", "A language learning companion app designed to build active vocabulary", "Langer helps me review my words every day."),
        "happy" to SeedWord("happy", "/ˈhæpi/", "Feeling or showing pleasure or contentment", "The good news made everyone happy."),
        "smart" to SeedWord("smart", "/smɑːt/", "Having or showing a quick-witted intelligence", "She came up with a very smart solution."),
        "create" to SeedWord("create", "/kriˈeɪt/", "To bring something into existence", "The artist wanted to create a masterpiece."),
        "think" to SeedWord("think", "/θɪŋk/", "To have a particular opinion, belief, or idea about something", "I think we should start the project today."),
        "write" to SeedWord("write", "/raɪt/", "To mark letters or words on a surface, typically paper or a screen", "He plans to write a book about his travels."),
        "speak" to SeedWord("speak", "/spiːk/", "To say something in order to convey information or express a feeling", "They speak three languages fluently."),
        "develop" to SeedWord("develop", "/dɪˈveləp/", "To grow or cause to grow and become more mature or advanced", "The team worked hard to develop the software."),
        "design" to SeedWord("design", "/dɪˈzaɪn/", "To decide upon the look and functioning of a building, garment, or object", "She was hired to design the new mobile app icon."),
        "system" to SeedWord("system", "/ˈsɪstəm/", "A set of things working together as parts of a mechanism or network", "We need a better system to manage our vocabulary decks.")
    )
}
