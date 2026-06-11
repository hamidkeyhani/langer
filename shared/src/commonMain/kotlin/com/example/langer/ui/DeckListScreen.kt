package com.example.langer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    dailyLimit: Int,
    onUpdateDailyLimit: (Int) -> Unit,
    onStudyDeck: (String) -> Unit,
    onManageDeck: (String) -> Unit,
    onBulkImport: (String) -> Unit,
    onCreateDeck: (String, String) -> Unit,
    onDeleteDeck: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSwitchDeckDialog by remember { mutableStateOf(false) }
    var deckToDelete by remember { mutableStateOf<Deck?>(null) }
    var deckName by remember { mutableStateOf("") }
    var deckDesc by remember { mutableStateOf("") }

    val now = currentTimeMillis()

    // Manage active deck state (default to the first deck)
    var activeDeckId by remember(decks) {
        mutableStateOf(decks.firstOrNull()?.id ?: "")
    }
    val activeDeck = remember(decks, activeDeckId) {
        decks.find { it.id == activeDeckId } ?: decks.firstOrNull()
    }

    // Category pills state
    val categories = listOf("Brainstorm", "Books", "Video", "Grammar")
    var selectedCategory by remember { mutableStateOf("Brainstorm") }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Langer",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.WbSunny else Icons.Default.NightsStay,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
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
                .padding(horizontal = 20.dp)
        ) {
            // "Choose what to learn today?" Header
            Text(
                "Choose what\nto learn today?",
                style = MaterialTheme.typography.headlineLarge.copy(lineHeight = 38.sp),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Horizontal Category Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { category ->
                    val isSelected = category == selectedCategory
                    val pillBg = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface
                    val pillText = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(pillBg)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            category,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = pillText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Featured Card (Active Deck)
            if (activeDeck != null) {
                val activeCards = cards.filter { it.deckId == activeDeck.id }
                val activeNew = activeCards.count { it.repetitions == 0 }
                val activeReview = activeCards.count { it.repetitions > 0 && it.nextReviewTimeMillis <= now }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = BrainBobIndigo)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left details column
                        Column(
                            modifier = Modifier.weight(0.58f).fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    activeDeck.name,
                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Learn $activeNew new • $activeReview review words",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            // Start Study Button (White Pill)
                            Button(
                                onClick = { onStudyDeck(activeDeck.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                shape = RoundedCornerShape(50.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "Start",
                                    color = BrainBobIndigo,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = BrainBobIndigo,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Right illustration column
                        Box(
                            modifier = Modifier.weight(0.42f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedUnicornCharacter(
                                modifier = Modifier.fillMaxSize().padding(4.dp)
                            )
                        }
                    }
                }
            } else {
                // Empty state active card
                Card(
                    modifier = Modifier.fillMaxWidth().height(180.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No decks available", fontWeight = FontWeight.Bold)
                            TextButton(onClick = { showCreateDialog = true }) {
                                Text("Create Deck Now")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recommended Features Section
            Text(
                "Recommended",
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (activeDeck != null) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        RecommendedActionRow(
                            title = "Manage Cards",
                            subtitle = "Search, edit or delete vocabulary",
                            iconColor = ActionColors.Blue,
                            icon = Icons.Default.List,
                            onClick = { onManageDeck(activeDeck.id) }
                        )
                    }
                    item {
                        RecommendedActionRow(
                            title = "Bulk Import",
                            subtitle = "Import lists of words instantly",
                            iconColor = ActionColors.Orange,
                            icon = Icons.Default.Share, // Upload arrow equivalent
                            onClick = { onBulkImport(activeDeck.id) }
                        )
                    }
                    item {
                        RecommendedActionRow(
                            title = "Add New Word",
                            subtitle = "Add a custom vocabulary card",
                            iconColor = ActionColors.Red,
                            icon = Icons.Default.Add,
                            onClick = { onManageDeck(activeDeck.id) } // goes to card manager where they tap FAB
                        )
                    }
                    item {
                        RecommendedActionRow(
                            title = "Switch Deck",
                            subtitle = "Study or manage other decks (${decks.size} total)",
                            iconColor = ActionColors.Green,
                            icon = Icons.Default.Refresh,
                            onClick = { showSwitchDeckDialog = true }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    // Decks switching / selection dialog
    if (showSwitchDeckDialog) {
        AlertDialog(
            onDismissRequest = { showSwitchDeckDialog = false },
            title = { Text("Switch Active Deck", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(decks) { deck ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeDeckId = deck.id
                                    showSwitchDeckDialog = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (deck.id == activeDeckId) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(deck.name, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    if (deck.description.isNotBlank()) {
                                        Text(deck.description, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                Row {
                                    if (deck.id == activeDeckId) {
                                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    IconButton(
                                        onClick = { deckToDelete = deck },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSwitchDeckDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Create Deck dialog
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

    // Delete confirmation dialog
    deckToDelete?.let { deck ->
        AlertDialog(
            onDismissRequest = { deckToDelete = null },
            title = { Text("Delete Deck") },
            text = { Text("Are you sure you want to delete the deck '${deck.name}'? This will permanently delete all of its cards.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDeck(deck.id)
                        if (activeDeckId == deck.id) {
                            activeDeckId = decks.firstOrNull { it.id != deck.id }?.id ?: ""
                        }
                        deckToDelete = null
                        showSwitchDeckDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SrsColors.Again)
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

    // Settings dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Daily New Words Limit",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                if (dailyLimit > 5) {
                                    onUpdateDailyLimit(dailyLimit - 5)
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        
                        Spacer(modifier = Modifier.width(24.dp))
                        
                        Text(
                            text = "$dailyLimit",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.width(24.dp))
                        
                        IconButton(
                            onClick = {
                                if (dailyLimit < 100) {
                                    onUpdateDailyLimit(dailyLimit + 5)
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Limit new words introduced per day (Max 100).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

@Composable
fun RecommendedActionRow(
    title: String,
    subtitle: String,
    iconColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Rounded Icon Container
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Arrow Right icon
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
