package com.example.langer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.langer.model.Flashcard
import com.example.langer.model.LocalAiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


enum class ValidationStatus {
    Idle,
    Validating,
    Valid,
    Invalid
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    deckId: String,
    cardId: String?,
    allCards: List<Flashcard>,
    onSave: (word: String, phonetic: String, meaning: String, example: String) -> Unit,
    onBack: () -> Unit
) {
    val existingCard = remember(cardId, allCards) {
        if (cardId != null) allCards.find { it.id == cardId } else null
    }

    var word by remember { mutableStateOf(existingCard?.word ?: "") }
    var phonetic by remember { mutableStateOf(existingCard?.phonetic ?: "") }
    var meaning by remember { mutableStateOf(existingCard?.meaning ?: "") }
    var example by remember { mutableStateOf(existingCard?.example ?: "") }

    var wordError by remember { mutableStateOf(false) }
    var meaningError by remember { mutableStateOf(false) }
    var validationState by remember { mutableStateOf(ValidationStatus.Idle) }

    val coroutineScope = rememberCoroutineScope()

    val isEditing = cardId != null

    // Debounced automatic trigger for known dictionary words
    LaunchedEffect(word) {
        if (word.isNotBlank() && !isEditing) {
            delay(2000)
            validationState = ValidationStatus.Validating
            delay(800) // Simulated loading search duration
            if (LocalAiModel.isKnownWord(word)) {
                validationState = ValidationStatus.Valid
                delay(200)
                try {
                    val generated = LocalAiModel.generateWordDetails(word)
                    if (generated.word.isNotBlank()) {
                        suspend fun animateTyping(target: String, update: (String) -> Unit) {
                            var current = ""
                            for (char in target) {
                                current += char
                                update(current)
                                delay(10)
                            }
                        }
                        
                        phonetic = ""
                        meaning = ""
                        example = ""
                        
                        animateTyping(generated.phonetic) { phonetic = it }
                        delay(120)
                        animateTyping(generated.meaning) { meaning = it }
                        delay(120)
                        animateTyping(generated.example) { example = it }
                    }
                } catch (e: Exception) {
                    // ignore
                }
            } else {
                validationState = ValidationStatus.Invalid
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Word" else "Add New Word", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Word Input
            OutlinedTextField(
                value = word,
                onValueChange = {
                    word = it
                    if (it.isNotBlank()) wordError = false
                    if (validationState != ValidationStatus.Idle) {
                        validationState = ValidationStatus.Idle
                    }
                },
                label = { Text("Word *") },
                placeholder = { Text("e.g. agree") },
                isError = wordError || validationState == ValidationStatus.Invalid,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (validationState == ValidationStatus.Valid) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = if (validationState == ValidationStatus.Valid) Color(0xFF10B981) else MaterialTheme.colorScheme.outline,
                    focusedLabelColor = if (validationState == ValidationStatus.Valid) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = if (validationState == ValidationStatus.Valid) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                supportingText = {
                    if (wordError) {
                        Text("Word cannot be empty", color = MaterialTheme.colorScheme.error)
                    } else if (validationState == ValidationStatus.Invalid) {
                        Text("Please enter a valid English word or use manual confirm", color = MaterialTheme.colorScheme.error)
                    } else if (validationState == ValidationStatus.Valid) {
                        Text("Word verified in dictionary", color = Color(0xFF10B981))
                    }
                },
                trailingIcon = {
                    if (word.isNotBlank()) {
                        if (validationState == ValidationStatus.Validating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        validationState = ValidationStatus.Validating
                                        delay(800)
                                        try {
                                            val isKnown = LocalAiModel.isKnownWord(word)
                                            val generated = LocalAiModel.generateWordDetails(word)
                                            if (generated.word.isNotBlank()) {
                                                suspend fun animateTyping(target: String, update: (String) -> Unit) {
                                                    var current = ""
                                                    for (char in target) {
                                                        current += char
                                                        update(current)
                                                        delay(10)
                                                    }
                                                }
                                                
                                                phonetic = ""
                                                meaning = ""
                                                example = ""
                                                
                                                validationState = if (isKnown) ValidationStatus.Valid else ValidationStatus.Idle
                                                animateTyping(generated.phonetic) { phonetic = it }
                                                delay(120)
                                                animateTyping(generated.meaning) { meaning = it }
                                                delay(120)
                                                animateTyping(generated.example) { example = it }
                                            } else {
                                                validationState = ValidationStatus.Invalid
                                            }
                                        } catch (e: Exception) {
                                            validationState = ValidationStatus.Invalid
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Confirm and Auto-Fill",
                                    tint = if (validationState == ValidationStatus.Valid) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Phonetics Input
            OutlinedTextField(
                value = phonetic,
                onValueChange = { phonetic = it },
                label = { Text("Phonetic Spelling (Optional)") },
                placeholder = { Text("e.g. ə'griː") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Meaning Input
            OutlinedTextField(
                value = meaning,
                onValueChange = {
                    meaning = it
                    if (it.isNotBlank()) meaningError = false
                },
                label = { Text("Meaning / Translation *") },
                placeholder = { Text("e.g. To have the same opinion as another person") },
                isError = meaningError,
                supportingText = {
                    if (meaningError) {
                        Text("Meaning cannot be empty", color = MaterialTheme.colorScheme.error)
                    }
                },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Example Sentence Input
            OutlinedTextField(
                value = example,
                onValueChange = { example = it },
                label = { Text("Example Sentence (Optional)") },
                placeholder = { Text("e.g. The students agree they have too much homework.") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    var hasError = false
                    if (word.isBlank()) {
                        wordError = true
                        hasError = true
                    }
                    if (meaning.isBlank()) {
                        meaningError = true
                        hasError = true
                    }
                    if (!hasError) {
                        onSave(word.trim(), phonetic.trim(), meaning.trim(), example.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    if (isEditing) "Save Changes" else "Add Word",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
