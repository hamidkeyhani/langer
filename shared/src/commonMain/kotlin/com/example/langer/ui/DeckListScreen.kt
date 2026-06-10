package com.example.langer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.langer.model.Deck
import com.example.langer.model.Flashcard
import com.example.langer.util.currentTimeMillis

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    decks: List<Deck>,
    cards: List<Flashcard>,
    onStudyDeck: (String) -> Unit,
    onManageDeck: (String) -> Unit,
    onBulkImport: (String) -> Unit,
    onCreateDeck: (String, String) -> Unit,
    onDeleteDeck: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }
    var deckName by remember { mutableStateOf("") }
    var deckDesc by remember { mutableStateOf("") }

    val now = currentTimeMillis()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Langer",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Create Deck")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Welcome Header & Stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            "Welcome Back!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Ready for your daily learning session?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Summary Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val newCount = cards.count { it.repetitions == 0 }
                            val reviewCount = cards.count { it.repetitions > 0 && it.nextReviewTimeMillis <= now }
                            
                            StatItem(title = "New Cards", value = "$newCount", color = MaterialTheme.colorScheme.secondary)
                            StatItem(title = "Reviews Due", value = "$reviewCount", color = SrsColors.Good)
                            StatItem(title = "Total Vocabulary", value = "${cards.size}", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Text(
                "My Decks",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (decks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No decks found",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Create a deck to start learning!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showCreateDialog = true }) {
                            Text("Create First Deck")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(decks) { deck ->
                        val deckCards = cards.filter { it.deckId == deck.id }
                        val deckNew = deckCards.count { it.repetitions == 0 }
                        val deckReview = deckCards.count { it.repetitions > 0 && it.nextReviewTimeMillis <= now }
                        
                        DeckRow(
                            deck = deck,
                            newCount = deckNew,
                            reviewCount = deckReview,
                            totalCount = deckCards.size,
                            onStudy = { onStudyDeck(deck.id) },
                            onManage = { onManageDeck(deck.id) },
                            onBulkImport = { onBulkImport(deck.id) },
                            onDelete = { deckToDelete = deck }
                        )

                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Custom Deck") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextField(
                        value = deckName,
                        onValueChange = { deckName = it },
                        label = { Text("Deck Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = deckDesc,
                        onValueChange = { deckDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (deckName.isNotBlank()) {
                            onCreateDeck(deckName, deckDesc)
                            deckName = ""
                            deckDesc = ""
                            showCreateDialog = false
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    deckToDelete?.let { deck ->
        AlertDialog(
            onDismissRequest = { deckToDelete = null },
            title = { Text("Delete Deck") },
            text = { Text("Are you sure you want to delete the deck '${deck.name}'? This will permanently delete all of its cards.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDeck(deck.id)
                        deckToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { deckToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}


@Composable
fun StatItem(title: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun DeckRow(
    deck: Deck,
    newCount: Int,
    reviewCount: Int,
    totalCount: Int,
    onStudy: () -> Unit,
    onManage: () -> Unit,
    onBulkImport: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (newCount > 0 || reviewCount > 0) {
                    onStudy()
                } else {
                    onManage()
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        deck.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (deck.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            deck.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Deck options")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Study Deck") },
                            leadingIcon = { Icon(Icons.Default.PlayArrow, null) },
                            onClick = {
                                showMenu = false
                                onStudy()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Manage Cards") },
                            leadingIcon = { Icon(Icons.Default.List, null) },
                            onClick = {
                                showMenu = false
                                onManage()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Bulk Import Cards") },
                            leadingIcon = { Icon(Icons.Default.Share, null) }, // Upload icon analog
                            onClick = {
                                showMenu = false
                                onBulkImport()
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Delete Deck", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Badge(text = "New: $newCount", color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.secondary)
                    Badge(text = "Due: $reviewCount", color = SrsColors.Good.copy(alpha = 0.15f), textColor = SrsColors.Good)
                    Badge(text = "Total: $totalCount", color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), textColor = MaterialTheme.colorScheme.primary)
                }

                if (newCount > 0 || reviewCount > 0) {
                    Button(
                        onClick = onStudy,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Study", fontWeight = FontWeight.Bold)
                    }
                } else {
                    TextButton(onClick = onManage) {
                        Text("Add Words")
                        Icon(Icons.Default.KeyboardArrowRight, null)
                    }
                }
            }
        }
    }
}

@Composable
fun Badge(text: String, color: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
