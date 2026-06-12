package com.example.langer.storage

import com.example.langer.model.Flashcard
import com.example.langer.model.Deck
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer

interface KeyValueStorage {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

expect fun getPlatformStorage(): KeyValueStorage

class LangerStorage(private val storage: KeyValueStorage) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false // compact for storage efficiency
    }

    fun getDecks(): List<Deck> {
        val str = storage.getString("langer_decks") ?: return emptyList()
        return try {
            json.decodeFromString(ListSerializer(Deck.serializer()), str)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveDecks(decks: List<Deck>) {
        val str = json.encodeToString(ListSerializer(Deck.serializer()), decks)
        storage.putString("langer_decks", str)
    }

    fun getCards(): List<Flashcard> {
        val str = storage.getString("langer_cards") ?: return emptyList()
        return try {
            json.decodeFromString(ListSerializer(Flashcard.serializer()), str)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCards(cards: List<Flashcard>) {
        val str = json.encodeToString(ListSerializer(Flashcard.serializer()), cards)
        storage.putString("langer_cards", str)
    }

    fun getThemePreference(): Boolean {
        return storage.getString("langer_dark_theme")?.toBoolean() ?: true
    }

    fun saveThemePreference(isDark: Boolean) {
        storage.putString("langer_dark_theme", isDark.toString())
    }

    fun getDailyNewCardsLimit(): Int {
        return storage.getString("langer_daily_new_cards_limit")?.toIntOrNull() ?: 20
    }

    fun saveDailyNewCardsLimit(limit: Int) {
        storage.putString("langer_daily_new_cards_limit", limit.toString())
    }

    fun getCategories(): List<String> {
        val str = storage.getString("langer_categories") ?: return listOf("Brainstorm", "Books", "Video", "Grammar")
        return try {
            json.decodeFromString(ListSerializer(serializer<String>()), str)
        } catch (e: Exception) {
            listOf("Brainstorm", "Books", "Video", "Grammar")
        }
    }

    fun saveCategories(categories: List<String>) {
        val str = json.encodeToString(ListSerializer(serializer<String>()), categories)
        storage.putString("langer_categories", str)
    }

    fun getSelectedCategory(): String? {
        return storage.getString("langer_selected_category")
    }

    fun saveSelectedCategory(category: String) {
        storage.putString("langer_selected_category", category)
    }

    fun getSelectedDeckId(): String? {
        return storage.getString("langer_selected_deck_id")
    }

    fun saveSelectedDeckId(deckId: String) {
        storage.putString("langer_selected_deck_id", deckId)
    }

    fun getSelectedDeckIdForCategory(category: String): String? {
        return storage.getString("langer_selected_deck_id_$category")
    }

    fun saveSelectedDeckIdForCategory(category: String, deckId: String) {
        storage.putString("langer_selected_deck_id_$category", deckId)
    }
}
