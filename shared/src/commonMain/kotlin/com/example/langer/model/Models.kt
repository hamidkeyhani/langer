package com.example.langer.model

import kotlinx.serialization.Serializable
import com.example.langer.util.currentTimeMillis

@Serializable
enum class CardRating {
    AGAIN,
    HARD,
    GOOD,
    EASY
}

@Serializable
data class Flashcard(
    val id: String = generateId(),
    val deckId: String,
    val word: String,
    val phonetic: String = "",
    val meaning: String,
    val example: String = "",
    val imageUrl: String = "",
    val easeFactor: Float = 2.5f,
    val repetitions: Int = 0,
    val intervalDays: Int = 0,
    val nextReviewTimeMillis: Long = currentTimeMillis(),
    val createdAtMillis: Long = currentTimeMillis(),
    val firstStudiedTimeMillis: Long = 0L,
    val lastStudiedTimeMillis: Long = 0L
)

@Serializable
data class SeedWord(
    val word: String,
    val phonetic: String = "",
    val meaning: String,
    val example: String = ""
)

@Serializable
data class Deck(
    val id: String = generateId(),
    val name: String,
    val description: String = "",
    val category: String = "Brainstorm",
    val dailyLimit: Int = 20
)

object SrsEngine {
    /**
     * Updates the spaced repetition stats for a flashcard based on the rating.
     * Returns the updated flashcard.
     */
    fun review(card: Flashcard, rating: CardRating, currentTimeMillis: Long = currentTimeMillis()): Flashcard {
        val q = when (rating) {
            CardRating.AGAIN -> 1
            CardRating.HARD -> 3
            CardRating.GOOD -> 4
            CardRating.EASY -> 5
        }

        var newEaseFactor = card.easeFactor
        var newRepetitions = card.repetitions
        var newIntervalDays = card.intervalDays

        if (q < 3) {
            // Again: reset interval and repetitions, decrease ease factor
            newRepetitions = 0
            newIntervalDays = 0 // 0 means review today/next session (same day)
            newEaseFactor = (card.easeFactor - 0.2f).coerceAtLeast(1.3f)
        } else {
            // Correct answer
            if (newRepetitions == 0) {
                newIntervalDays = when (rating) {
                    CardRating.HARD -> 1
                    CardRating.GOOD -> 1
                    CardRating.EASY -> 3 // graduated to 3 days
                    else -> 1
                }
            } else if (newRepetitions == 1) {
                newIntervalDays = when (rating) {
                    CardRating.HARD -> 2
                    CardRating.GOOD -> 4
                    CardRating.EASY -> 6
                    else -> 4
                }
            } else {
                val multiplier = when (rating) {
                    CardRating.HARD -> 1.2f
                    CardRating.GOOD -> newEaseFactor
                    CardRating.EASY -> newEaseFactor * 1.3f
                    else -> newEaseFactor
                }
                newIntervalDays = (card.intervalDays * multiplier).toInt().coerceAtLeast(1)
            }

            // Adjust ease factor
            // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
            val efAdjustment = 0.1f - (5f - qf(q)) * (0.08f + (5f - qf(q)) * 0.02f)
            newEaseFactor = (card.easeFactor + efAdjustment).coerceIn(1.3f, 3.0f)
            newRepetitions += 1
        }

        // Calculate next review timestamp (rounded to start/end of day or simple interval)
        val dayInMillis = 24L * 60 * 60 * 1000
        val nextReview = if (newIntervalDays == 0) {
            // Review again very soon (e.g. in 1 minute, but for simple offline study, we show in same session)
            currentTimeMillis
        } else {
            currentTimeMillis + (newIntervalDays * dayInMillis)
        }

        return card.copy(
            easeFactor = newEaseFactor,
            repetitions = newRepetitions,
            intervalDays = newIntervalDays,
            nextReviewTimeMillis = nextReview
        )
    }

    private fun qf(q: Int): Float = q.toFloat()
}

fun generateId(): String {
    val chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val randomPart = (1..8).map { chars.random() }.joinToString("")
    return "${currentTimeMillis()}-$randomPart"
}
