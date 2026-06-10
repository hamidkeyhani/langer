package com.example.langer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.langer.model.CardRating
import com.example.langer.model.Flashcard
import com.example.langer.model.SrsEngine
import com.example.langer.tts.TtsPlayer
import com.example.langer.util.currentTimeMillis
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckId: String,
    deckName: String,
    allCards: List<Flashcard>,
    onSaveCard: (Flashcard) -> Unit,
    onBack: () -> Unit
) {
    val now = currentTimeMillis()
    
    // Filter cards belonging to this deck that are new or due
    val initialStudyCards = remember(deckId, allCards) {
        allCards.filter { it.deckId == deckId && (it.repetitions == 0 || it.nextReviewTimeMillis <= now) }
    }

    // Keep track of the active queue for this study session
    val sessionQueue = remember { mutableStateListOf<Flashcard>() }
    var sessionInitialized by remember { mutableStateOf(false) }

    if (!sessionInitialized) {
        sessionQueue.clear()
        sessionQueue.addAll(initialStudyCards.shuffled())
        sessionInitialized = true
    }

    // Counters for completion screen
    var studiedCount by remember { mutableStateOf(0) }
    var againCount by remember { mutableStateOf(0) }
    var goodCount by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(deckName, fontWeight = FontWeight.Bold) },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (sessionQueue.isEmpty()) {
                // Celebration Screen
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(100.dp),
                        tint = SrsColors.Good
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "Congratulations!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "You have finished this deck for now.",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Daily limit reached. All due cards reviewed.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Session Stats Card
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Studied", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("$studiedCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Again (Forgot)", style = MaterialTheme.typography.labelLarge, color = SrsColors.Again)
                                Text("$againCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SrsColors.Again)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Good/Easy", style = MaterialTheme.typography.labelLarge, color = SrsColors.Good)
                                Text("$goodCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SrsColors.Good)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedButton(onClick = onBack) {
                            Text("Back to Decks")
                        }
                        // Custom study: reset some cards for studying again
                        if (allCards.any { it.deckId == deckId }) {
                            Button(
                                onClick = {
                                    // Custom Study: Reload all cards in the deck for practice (temporary session)
                                    val practiceCards = allCards.filter { it.deckId == deckId }
                                    sessionQueue.clear()
                                    sessionQueue.addAll(practiceCards.shuffled())
                                    studiedCount = 0
                                    againCount = 0
                                    goodCount = 0
                                }
                            ) {
                                Text("Practice Deck Again")
                            }
                        }
                    }
                }
            } else {
                val currentCard = sessionQueue.first()
                var isFlipped by remember(currentCard.id) { mutableStateOf(false) }

                // 3D Card Rotation State
                val rotation by animateFloatAsState(
                    targetValue = if (isFlipped) 180f else 0f,
                    animationSpec = tween(durationMillis = 500)
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Progress bar
                    val totalCards = initialStudyCards.size.coerceAtLeast(1)
                    val remaining = sessionQueue.size
                    val progress = 1f - (remaining.toFloat() / totalCards.toFloat()).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier.weight(1f).height(8.dp).graphicsLayer(shape = RoundedCornerShape(4.dp), clip = true),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "$remaining cards left",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }

                    // 3D Flashcard container
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12 * density
                            }
                            .background(Color.Transparent)
                    ) {
                        if (rotation <= 90f) {
                            // FRONT SIDE
                            Card(
                                modifier = Modifier.fillMaxSize(),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.outline,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        currentCard.word,
                                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    if (currentCard.phonetic.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            currentCard.phonetic,
                                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontStyle = FontStyle.Italic,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    // Play audio button
                                    IconButton(
                                        onClick = { TtsPlayer.speak(currentCard.word) },
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        ),
                                        modifier = Modifier.size(56.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Speak Word",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(48.dp))
                                    Text(
                                        "Tap anywhere to show answer",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // BACK SIDE (Rotated back by 180 degrees so it renders right side up)
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        rotationY = 180f
                                    },
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.secondary,
                                            MaterialTheme.colorScheme.primary
                                        )
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(24.dp)
                                ) {
                                    // Card Word Header (smaller than front)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                currentCard.word,
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (currentCard.phonetic.isNotBlank()) {
                                                Text(
                                                    currentCard.phonetic,
                                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                                                    color = MaterialTheme.colorScheme.secondary,
                                                    fontStyle = FontStyle.Italic
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { TtsPlayer.speak(currentCard.word) },
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            ),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Icon(Icons.Default.PlayArrow, "Speak Word", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 16.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                                    // Scrollable content area
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Meaning
                                        Column {
                                            Text(
                                                "Meaning",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                currentCard.meaning,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        // Example
                                        if (currentCard.example.isNotBlank()) {
                                            Column {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        "Example Sentence",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    IconButton(
                                                        onClick = { TtsPlayer.speak(currentCard.example) },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            Icons.Default.PlayArrow,
                                                            "Speak Example",
                                                            tint = MaterialTheme.colorScheme.secondary
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                // Make target word bold in the example sentence
                                                val primaryColor = MaterialTheme.colorScheme.primary
                                                val annotatedExample = remember(currentCard.example, currentCard.word, primaryColor) {
                                                    buildAnnotatedString {
                                                        val text = currentCard.example
                                                        val word = currentCard.word
                                                        val index = text.indexOf(word, ignoreCase = true)
                                                        if (index >= 0) {
                                                            append(text.substring(0, index))
                                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor)) {
                                                                append(text.substring(index, index + word.length))
                                                            }
                                                            append(text.substring(index + word.length))
                                                        } else {
                                                            append(text)
                                                        }
                                                    }
                                                }
                                                Text(
                                                    annotatedExample,
                                                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontStyle = FontStyle.Italic),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Invisible clickable overlay to trigger flip (only when not flipped)
                        if (!isFlipped) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { isFlipped = true }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    if (!isFlipped) {
                        Button(
                            onClick = { isFlipped = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Show Answer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Rating buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Again
                            val nextAgain = SrsEngine.review(currentCard, CardRating.AGAIN)
                            RatingButton(
                                title = "Again",
                                interval = "<1m",
                                color = SrsColors.Again,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    studiedCount++
                                    againCount++
                                    // Update SRS state but keep card in queue
                                    val updatedCard = SrsEngine.review(currentCard, CardRating.AGAIN)
                                    onSaveCard(updatedCard)
                                    // Move card to the end of the session queue
                                    sessionQueue.removeFirst()
                                    sessionQueue.add(updatedCard)
                                }
                            )

                            // Hard
                            val nextHard = SrsEngine.review(currentCard, CardRating.HARD)
                            val intervalHard = formatInterval(nextHard.intervalDays)
                            RatingButton(
                                title = "Hard",
                                interval = intervalHard,
                                color = SrsColors.Hard,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    studiedCount++
                                    val updatedCard = SrsEngine.review(currentCard, CardRating.HARD)
                                    onSaveCard(updatedCard)
                                    sessionQueue.removeFirst()
                                }
                            )

                            // Good
                            val nextGood = SrsEngine.review(currentCard, CardRating.GOOD)
                            val intervalGood = formatInterval(nextGood.intervalDays)
                            RatingButton(
                                title = "Good",
                                interval = intervalGood,
                                color = SrsColors.Good,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    studiedCount++
                                    goodCount++
                                    val updatedCard = SrsEngine.review(currentCard, CardRating.GOOD)
                                    onSaveCard(updatedCard)
                                    sessionQueue.removeFirst()
                                }
                            )

                            // Easy
                            val nextEasy = SrsEngine.review(currentCard, CardRating.EASY)
                            val intervalEasy = formatInterval(nextEasy.intervalDays)
                            RatingButton(
                                title = "Easy",
                                interval = intervalEasy,
                                color = SrsColors.Easy,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    studiedCount++
                                    goodCount++
                                    val updatedCard = SrsEngine.review(currentCard, CardRating.EASY)
                                    onSaveCard(updatedCard)
                                    sessionQueue.removeFirst()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingButton(
    title: String,
    interval: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
        modifier = modifier.height(56.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                interval,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

fun formatInterval(days: Int): String {
    return when {
        days == 0 -> "<1m"
        days < 30 -> "${days}d"
        days < 365 -> "${(days / 30f).roundToInt()}m"
        else -> "${(days / 365f).roundToInt()}y"
    }
}
