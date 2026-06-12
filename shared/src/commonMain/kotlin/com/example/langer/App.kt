package com.example.langer

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import langer.shared.generated.resources.Res
import kotlinx.serialization.json.Json
import com.example.langer.model.Deck
import com.example.langer.model.Flashcard
import com.example.langer.model.SeedWord
import com.example.langer.storage.LangerStorage
import com.example.langer.storage.getPlatformStorage
import com.example.langer.ui.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun App(onExit: () -> Unit = {}) {
    val storage = remember { LangerStorage(getPlatformStorage()) }
    val decksState = remember { mutableStateListOf<Deck>() }
    val cardsState = remember { mutableStateListOf<Flashcard>() }
    val categoriesState = remember { mutableStateListOf<String>() }
    
    var isDarkTheme by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Seed and Load initial data
    LaunchedEffect(Unit) {
        // Load settings preference
        isDarkTheme = storage.getThemePreference()
        categoriesState.addAll(storage.getCategories())

        // 1. Read and Parse JSON with ignoreUnknownKeys = true to avoid strict deserialization errors
        val jsonText = try {
            Res.readBytes("files/essential_words.json").decodeToString()
        } catch (e: Exception) {
            ""
        }
        val jsonParser = Json { ignoreUnknownKeys = true }
        val seedWords = try {
            if (jsonText.isNotEmpty()) {
                jsonParser.decodeFromString<List<SeedWord>>(jsonText)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }

        var loadedDecks = storage.getDecks()
        var loadedCards = storage.getCards()

        // 2. Force re-seed/synchronization if:
        //    - Database is empty (first launch)
        //    - Loaded cards is <= 5 (meaning it previously fell back to the 5 default cards due to JSON parsing error)
        //    - Loaded cards size does not match seedWords size (meaning JSON was changed/updated by the user)
        val shouldReSeed = loadedDecks.isEmpty() || loadedCards.size <= 5 || (seedWords.isNotEmpty() && loadedCards.size != seedWords.size)

        if (shouldReSeed) {
            val defaultDeck = Deck(
                name = "Essential English Words",
                description = "Complete essential academic vocabulary list"
            )
            loadedDecks = listOf(defaultDeck)
            
            loadedCards = if (seedWords.isNotEmpty()) {
                seedWords.map { seed ->
                    Flashcard(
                        deckId = defaultDeck.id,
                        word = seed.word,
                        phonetic = seed.phonetic,
                        meaning = seed.meaning,
                        example = seed.example
                    )
                }
            } else {
                listOf(
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
            }
            
            storage.saveDecks(loadedDecks)
            storage.saveCards(loadedCards)
        }

        decksState.clear()
        decksState.addAll(loadedDecks)
        
        cardsState.clear()
        cardsState.addAll(loadedCards)
        
        isLoading = false
    }

    LangerTheme(darkTheme = isDarkTheme) {
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit Langer") },
                text = { Text("Are you sure you want to exit Langer?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onExit()
                        }
                    ) {
                        Text("Exit")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (!isLoading) {
            val navigator = rememberNavigator(Screen.DeckList)

            RegisterBackHandler(enabled = true) {
                if (navigator.canGoBack) {
                    navigator.pop()
                } else {
                    showExitDialog = true
                }
            }

            AnimatedNavigation(navigator) { screen ->
                when (screen) {
                    is Screen.DeckList -> {
                        DeckListScreen(
                            decks = decksState,
                            cards = cardsState,
                            categories = categoriesState,
                            onAddCategory = { newCategory ->
                                if (!categoriesState.contains(newCategory)) {
                                    categoriesState.add(newCategory)
                                    storage.saveCategories(categoriesState.toList())
                                }
                            },
                            isDarkTheme = isDarkTheme,
                            onToggleTheme = {
                                isDarkTheme = !isDarkTheme
                                storage.saveThemePreference(isDarkTheme)
                            },
                            onUpdateDeckLimit = { deckId, limit ->
                                val index = decksState.indexOfFirst { it.id == deckId }
                                if (index != -1) {
                                    val updatedDeck = decksState[index].copy(dailyLimit = limit)
                                    decksState[index] = updatedDeck
                                    storage.saveDecks(decksState.toList())
                                }
                            },
                            onStudyDeck = { navigator.navigateTo(Screen.Study(it)) },
                            onAddCard = { navigator.navigateTo(Screen.AddEditCard(it, null)) },
                            onManageDeck = { navigator.navigateTo(Screen.CardManager(it)) },
                            onBulkImport = { navigator.navigateTo(Screen.BulkImport(it)) },
                            onCreateDeck = { name, desc, category ->
                                val newDeck = Deck(name = name, description = desc, category = category)
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
                            dailyLimit = deck?.dailyLimit ?: 20,
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
                        val deck = decksState.find { it.id == screen.deckId }
                        AddEditCardScreen(
                            deckId = screen.deckId,
                            deckName = deck?.name ?: "Unknown Deck",
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
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.size(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedUnicornCharacter(modifier = Modifier.fillMaxSize())
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Langer",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Preparing your word deck...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    }
}