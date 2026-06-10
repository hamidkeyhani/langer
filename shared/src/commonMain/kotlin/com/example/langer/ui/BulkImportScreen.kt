package com.example.langer.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.langer.model.Flashcard
import com.example.langer.model.generateId
import kotlinx.datetime.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkImportScreen(
    deckId: String,
    deckName: String,
    onImportSuccess: (List<Flashcard>) -> Unit,
    onBack: () -> Unit
) {
    var importText by remember { mutableStateOf("") }
    var parsedPreviewList by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Parse the pasted text reactively to show a preview count to the user
    LaunchedEffect(importText) {
        if (importText.isBlank()) {
            parsedPreviewList = emptyList()
            errorMessage = null
            return@LaunchedEffect
        }

        val cardsList = mutableListOf<Flashcard>()
        val lines = importText.lines()
        var errorCount = 0

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isBlank()) continue

            // Split by pipe (|), semicolon (;), or tab
            val parts = when {
                trimmedLine.contains("|") -> trimmedLine.split("|")
                trimmedLine.contains(";") -> trimmedLine.split(";")
                trimmedLine.contains("\t") -> trimmedLine.split("\t")
                else -> listOf(trimmedLine)
            }

            val cleanedParts = parts.map { it.trim() }

            if (cleanedParts.size >= 2) {
                val word = cleanedParts[0]
                var phonetic = ""
                var meaning = ""
                var example = ""

                if (cleanedParts.size == 2) {
                    meaning = cleanedParts[1]
                } else if (cleanedParts.size == 3) {
                    meaning = cleanedParts[1]
                    example = cleanedParts[2]
                } else {
                    // 4-part format: word | phonetic | meaning | example
                    phonetic = cleanedParts[1]
                    meaning = cleanedParts[2]
                    example = cleanedParts[3]
                }

                if (word.isNotEmpty() && meaning.isNotEmpty()) {
                    cardsList.add(
                        Flashcard(
                            id = generateId(),
                            deckId = deckId,
                            word = word,
                            phonetic = phonetic,
                            meaning = meaning,
                            example = example
                        )
                    )
                } else {
                    errorCount++
                }
            } else {
                errorCount++
            }
        }

        parsedPreviewList = cardsList
        errorMessage = if (errorCount > 0) {
            "Skipped $errorCount invalid lines (could not parse)."
        } else {
            null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Bulk Import Words", fontWeight = FontWeight.Bold)
                        Text(
                            "Import to: $deckName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
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
            // Instructions Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("How to Import:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Paste a list of words. Use '|' or ';' to separate fields. You can paste one word per line in one of these formats:\n\n" +
                                "1. Word | Meaning\n" +
                                "2. Word | Meaning | Example sentence\n" +
                                "3. Word | Phonetic | Meaning | Example sentence\n\n" +
                                "Example:\n" +
                                "abundant | Existing in large quantities | Coal is abundant.\n" +
                                "benevolent | bə'nevələnt | Kind and helpful | A benevolent smile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }

            // Input TextArea
            OutlinedTextField(
                value = importText,
                onValueChange = {
                    importText = it
                    successMessage = null
                },
                placeholder = { Text("Paste your words here...") },
                label = { Text("Vocabulary List") },
                minLines = 8,
                maxLines = 15,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Info Alerts / Logs
            if (successMessage != null) {
                Text(
                    successMessage!!,
                    color = SrsColors.Good,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Parsed: ${parsedPreviewList.size} words ready",
                        fontWeight = FontWeight.Bold,
                        color = if (parsedPreviewList.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    errorMessage?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Import Button
            Button(
                onClick = {
                    if (parsedPreviewList.isNotEmpty()) {
                        onImportSuccess(parsedPreviewList)
                        successMessage = "Successfully imported ${parsedPreviewList.size} words!"
                        importText = ""
                    }
                },
                enabled = parsedPreviewList.isNotEmpty(),
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Import ${parsedPreviewList.size} Words", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
