package com.example.langer

import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import com.example.langer.model.Deck
import com.example.langer.model.Flashcard
import com.example.langer.storage.LangerStorage
import com.example.langer.storage.getPlatformStorage
import com.example.langer.ui.*

@Composable
@Preview
fun App() {
    val storage = remember { LangerStorage(getPlatformStorage()) }
    val decksState = remember { mutableStateListOf<Deck>() }
    val cardsState = remember { mutableStateListOf<Flashcard>() }
    var isLoading by remember { mutableStateOf(true) }

    // Seed and Load initial data
    LaunchedEffect(Unit) {
        var loadedDecks = storage.getDecks()
        var loadedCards = storage.getCards()

        if (loadedDecks.isEmpty()) {
            val defaultDeck = Deck(
                name = "Essential English Words",
                description = "Standard vocabulary for daily practice"
            )
            loadedDecks = listOf(defaultDeck)
            
            loadedCards = listOf(
                Flashcard(
                    deckId = defaultDeck.id,
                    word = "agree",
                    phonetic = "ə'griː",
                    meaning = "To have the same opinion or belief as another person.",
                    example = "The students agree they have too much homework."
                ),
                Flashcard(
                    deckId = defaultDeck.id,
                    word = "abundant",
                    phonetic = "ə'bʌndənt",
                    meaning = "Existing or available in large quantities; overflowing.",
                    example = "Coal is an abundant resource in this region."
                ),
                Flashcard(
                    deckId = defaultDeck.id,
                    word = "benevolent",
                    phonetic = "bə'nevələnt",
                    meaning = "Well meaning and kindly; charitable.",
                    example = "A benevolent gentleman left a large sum of money to the hospital."
                ),
                Flashcard(
                    deckId = defaultDeck.id,
                    word = "candid",
                    phonetic = "'kændɪd",
                    meaning = "Truthful and straightforward; frank.",
                    example = "His responses were remarkably candid and open."
                ),
                Flashcard(
                    deckId = defaultDeck.id,
                    word = "diligent",
                    phonetic = "'dɪlɪdʒənt",
                    meaning = "Having or showing care and conscientiousness in one's work or duties.",
                    example = "The diligent student was rewarded with top marks."
                )
            )
            
            storage.saveDecks(loadedDecks)
            storage.saveCards(loadedCards)
        }

        decksState.clear()
        decksState.addAll(loadedDecks)
        
        cardsState.clear()
        cardsState.addAll(loadedCards)
        
        isLoading = false
    }

    LangerTheme {
        if (!isLoading) {
            val navigator = rememberNavigator(Screen.DeckList)

            AnimatedNavigation(navigator) { screen ->
                when (screen) {
                    is Screen.DeckList -> {
                        DeckListScreen(
                            decks = decksState,
                            cards = cardsState,
                            onStudyDeck = { navigator.navigateTo(Screen.Study(it)) },
                            onManageDeck = { navigator.navigateTo(Screen.CardManager(it)) },
                            onBulkImport = { navigator.navigateTo(Screen.BulkImport(it)) },
                            onCreateDeck = { name, desc ->
                                val newDeck = Deck(name = name, description = desc)
                                decksState.add(newDeck)
                                storage.saveDecks(decksState.toList())
                            },
                            onDeleteDeck = { deckId ->
                                decksState.removeAll { it.id == deckId }
                                cardsState.removeAll { it.deckId == deckId }
                                storage.saveDecks(decksState.toList())
                                storage.saveCards(cardsState.toList())
                            }
                        )
                    }
                    is Screen.Study -> {
                        val deck = decksState.find { it.id == screen.deckId }
                        StudyScreen(
                            deckId = screen.deckId,
                            deckName = deck?.name ?: "Study",
                            allCards = cardsState,
                            onSaveCard = { card ->
                                val idx = cardsState.indexOfFirst { it.id == card.id }
                                if (idx >= 0) {
                                    cardsState[idx] = card
                                } else {
                                    cardsState.add(card)
                                }
                                storage.saveCards(cardsState.toList())
                            },
                            onBack = { navigator.pop() }
                        )
                    }
                    is Screen.CardManager -> {
                        val deck = decksState.find { it.id == screen.deckId }
                        CardManagerScreen(
                            deckId = screen.deckId,
                            deckName = deck?.name ?: "Cards",
                            cards = cardsState,
                            onAddCard = { navigator.navigateTo(Screen.AddEditCard(screen.deckId, null)) },
                            onEditCard = { cardId -> navigator.navigateTo(Screen.AddEditCard(screen.deckId, cardId)) },
                            onDeleteCard = { cardId ->
                                cardsState.removeAll { it.id == cardId }
                                storage.saveCards(cardsState.toList())
                            },
                            onBack = { navigator.pop() }
                        )
                    }
                    is Screen.AddEditCard -> {
                        AddEditCardScreen(
                            deckId = screen.deckId,
                            cardId = screen.cardId,
                            allCards = cardsState,
                            onSave = { word, phonetic, meaning, example ->
                                if (screen.cardId == null) {
                                    val newCard = Flashcard(
                                        deckId = screen.deckId,
                                        word = word,
                                        phonetic = phonetic,
                                        meaning = meaning,
                                        example = example
                                    )
                                    cardsState.add(newCard)
                                } else {
                                    val idx = cardsState.indexOfFirst { it.id == screen.cardId }
                                    if (idx >= 0) {
                                        cardsState[idx] = cardsState[idx].copy(
                                            word = word,
                                            phonetic = phonetic,
                                            meaning = meaning,
                                            example = example
                                        )
                                    }
                                }
                                storage.saveCards(cardsState.toList())
                                navigator.pop()
                            },
                            onBack = { navigator.pop() }
                        )
                    }
                    is Screen.BulkImport -> {
                        val deck = decksState.find { it.id == screen.deckId }
                        BulkImportScreen(
                            deckId = screen.deckId,
                            deckName = deck?.name ?: "Deck",
                            onImportSuccess = { newCards ->
                                cardsState.addAll(newCards)
                                storage.saveCards(cardsState.toList())
                                navigator.pop()
                            },
                            onBack = { navigator.pop() }
                        )
                    }
                }
            }
        }
    }
}