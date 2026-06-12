package com.example.langer.model

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import langer.shared.generated.resources.Res

@Serializable
data class DictionaryWord(
    val word: String,
    val phonetic: String? = null,
    val phonetics: List<DictionaryPhonetic> = emptyList(),
    val meanings: List<DictionaryMeaning> = emptyList()
)

@Serializable
data class DictionaryPhonetic(
    val text: String? = null,
    val audio: String? = null
)

@Serializable
data class DictionaryMeaning(
    val partOfSpeech: String,
    val definitions: List<DictionaryDefinition> = emptyList()
)

@Serializable
data class DictionaryDefinition(
    val definition: String,
    val example: String? = null
)

object LocalAiModel {
    private val jsonParser = Json { ignoreUnknownKeys = true }
    private var cachedSeedWords: List<SeedWord>? = null

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

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

    // Check if the word is in the essential words JSON, commonWordsSet, or query the online API
    suspend fun isKnownWord(inputWord: String): Boolean {
        val normalized = inputWord.trim().lowercase()
        if (normalized.isBlank()) return false
        
        // 1. Quick local checks
        val localWords = getSeedWords()
        if (localWords.any { it.word.trim().lowercase() == normalized }) return true
        if (commonWordsSet.contains(normalized)) return true
        
        // 2. Query online dictionary API
        return try {
            val response = client.get("https://api.dictionaryapi.dev/api/v2/entries/en/$normalized")
            response.status.value == 200
        } catch (e: Exception) {
            false
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

        // 2. Query online dictionary API
        try {
            val response = client.get("https://api.dictionaryapi.dev/api/v2/entries/en/$normalized")
            if (response.status.value == 200) {
                val wordsList = response.body<List<DictionaryWord>>()
                if (wordsList.isNotEmpty()) {
                    val dictWord = wordsList[0]
                    
                    // Extract phonetic
                    var phoneticText = dictWord.phonetic ?: ""
                    if (phoneticText.isBlank() && dictWord.phonetics.isNotEmpty()) {
                        phoneticText = dictWord.phonetics.find { !it.text.isNullOrBlank() }?.text ?: ""
                    }
                    if (phoneticText.isBlank()) {
                        phoneticText = generatePhonetic(normalized)
                    }

                    // Extract meaning and example
                    var meaningText = ""
                    var exampleText = ""
                    var firstPartOfSpeech = ""

                    if (dictWord.meanings.isNotEmpty()) {
                        firstPartOfSpeech = dictWord.meanings[0].partOfSpeech
                        for (meaning in dictWord.meanings) {
                            val defObj = meaning.definitions.find { it.definition.isNotBlank() }
                            if (defObj != null) {
                                meaningText = defObj.definition
                                exampleText = defObj.example ?: ""
                                break
                            }
                        }
                    }

                    if (meaningText.isBlank()) {
                        val fallback = generateMeaningAndExample(inputWord)
                        meaningText = fallback.first
                        exampleText = fallback.second
                    } else if (exampleText.isBlank()) {
                        exampleText = generateContextualExample(inputWord, meaningText, firstPartOfSpeech)
                    }

                    return SeedWord(
                        word = inputWord,
                        phonetic = phoneticText,
                        meaning = meaningText,
                        example = exampleText
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback on network/deserialization errors
        }

        // 3. Fallback: Rule-based AI generator for phonetic spelling
        val phonetic = generatePhonetic(normalized)

        // 4. Fallback: Rule-based AI generator for meaning and example sentence
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

    private fun generateContextualExample(word: String, meaning: String, partOfSpeech: String? = null): String {
        val w = word.lowercase().trim()
        val pos = partOfSpeech?.lowercase() ?: when {
            w.endsWith("ly") -> "adverb"
            w.endsWith("tion") || w.endsWith("ness") || w.endsWith("ment") -> "noun"
            w.endsWith("able") || w.endsWith("ible") || w.endsWith("ful") || w.endsWith("less") -> "adjective"
            w.endsWith("ify") || w.endsWith("ize") || w.endsWith("ise") -> "verb"
            else -> "noun"
        }

        return when (pos) {
            "noun" -> {
                if (w == "phone" || w == "laptop" || w == "computer" || w == "book" || w == "pen" || w == "car") {
                    "Can I borrow your $w for a minute?"
                } else {
                    "We need to find a better $w to solve this problem."
                }
            }
            "verb" -> "They decided to $w the plan immediately."
            "adjective" -> "The solution was highly $w and met all our requirements."
            "adverb" -> "She completed the assignment $w and without any errors."
            else -> "We need to discuss this $w in detail."
        }
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
                    generateContextualExample(word, "")
                )
            }
        }
    }

    private val commonWordsSet = setOf(
        "hell", "hello", "yes", "no", "good", "bad", "word", "card", "deck", "plane", "airplane", "book", "read", "speak", "talk", "write", "listen", "hear", "run", "walk", "go", "come", "take", "give", "make", "do", "see", "look", "find", "get", "know", "think", "work", "play", "live", "die", "love", "hate", "like", "want", "need", "help", "thank", "please", "sorry", "excuse", "welcome", "friend", "family", "home", "house", "room", "door", "window", "wall", "floor", "roof", "garden", "city", "town", "country", "world", "earth", "sky", "sun", "moon", "star", "cloud", "rain", "snow", "wind", "fire", "water", "air", "land", "sea", "river", "lake", "tree", "plant", "flower", "grass", "animal", "dog", "cat", "bird", "fish", "horse", "cow", "sheep", "pig", "chicken", "mouse", "fly", "bee", "ant", "spider", "snake", "frog", "apple", "banana", "orange", "grape", "peach", "pear", "melon", "lemon", "lime", "berry", "cherry", "plum", "apricot", "fig", "date", "nut", "seed", "bread", "butter", "cheese", "milk", "egg", "meat", "fish", "rice", "pasta", "soup", "salad", "salt", "pepper", "sugar", "honey", "tea", "coffee", "juice", "beer", "wine", "water", "phone", "laptop", "computer"
    )
}
