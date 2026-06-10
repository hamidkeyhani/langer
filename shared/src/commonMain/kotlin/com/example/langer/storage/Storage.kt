package com.example.langer.storage

import com.example.langer.model.Flashcard
import com.example.langer.model.Deck
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer

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
}
