package com.example.langer.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    deckId: String,
    deckName: String,
    allCards: List<Flashcard>,
    dailyLimit: Int,
    onSaveCard: (Flashcard) -> Unit,
    onBack: () -> Unit
) {
    val now = currentTimeMillis()
    
    // Helper to check if two timestamps fall on the same calendar day in the device's local timezone
    fun isSameDay(t1: Long, t2: Long): Boolean {
        val instant1 = Instant.fromEpochMilliseconds(t1)
        val instant2 = Instant.fromEpochMilliseconds(t2)
        val tz = TimeZone.currentSystemDefault()
        val date1 = instant1.toLocalDateTime(tz).date
        val date2 = instant2.toLocalDateTime(tz).date
        return date1 == date2
    }

    // Filter cards belonging to this deck (reactive derivedStateOf tracks modifications to the SnapshotStateList)
    val deckCards by remember(deckId) {
        derivedStateOf { allCards.filter { it.deckId == deckId } }
    }

    // Count how many new cards were introduced today (first study timestamp matches today)
    val newCardsStudiedToday by remember(now) {
        derivedStateOf { deckCards.count { it.firstStudiedTimeMillis > 0L && isSameDay(it.firstStudiedTimeMillis, now) } }
    }

    val newCardsAllowance by remember {
        derivedStateOf { (dailyLimit - newCardsStudiedToday).coerceAtLeast(0) }
    }

    // Scheduled reviews + lapsed reviews from previous days (failed reviews whose repetitions got reset)
    val dueReviews by remember(now) {
        derivedStateOf {
            deckCards.filter { 
                it.firstStudiedTimeMillis > 0L && 
                !isSameDay(it.firstStudiedTimeMillis, now) && 
                it.nextReviewTimeMillis <= now 
            }
        }
    }
    
    // New cards that were introduced today but not yet successfully graduated (repetitions == 0)
    val introducedTodayActive by remember(now) {
        derivedStateOf {
            deckCards.filter { 
                it.repetitions == 0 && 
                it.firstStudiedTimeMillis > 0L && 
                isSameDay(it.firstStudiedTimeMillis, now) 
            }
        }
    }

    // Completely untouched new cards (never studied before)
    val untouchedCards by remember {
        derivedStateOf { deckCards.filter { it.firstStudiedTimeMillis == 0L } }
    }

    // Keep track of the active queue for this study session
    val sessionQueue = remember { mutableStateListOf<Flashcard>() }
    var sessionInitialized by remember { mutableStateOf(false) }

    val initialStudyCards = remember(deckId) {
        val limitedUntouched = untouchedCards.shuffled().take(newCardsAllowance)
        (dueReviews + introducedTodayActive + limitedUntouched).shuffled()
    }

    if (!sessionInitialized) {
        sessionQueue.clear()
        sessionQueue.addAll(initialStudyCards)
        sessionInitialized = true
    }

    // Counters for completion screen
    var studiedCount by remember { mutableStateOf(0) }
    var againCount by remember { mutableStateOf(0) }
    var goodCount by remember { mutableStateOf(0) }

    // Cumulative calendar day stats for the completion screen (retrieved from database in local timezone)
    val todayStudiedCount by remember(now) {
        derivedStateOf { deckCards.count { it.lastStudiedTimeMillis > 0L && isSameDay(it.lastStudiedTimeMillis, now) } }
    }
    val todayAgainCount by remember(now) {
        derivedStateOf { deckCards.count { it.repetitions == 0 && it.lastStudiedTimeMillis > 0L && isSameDay(it.lastStudiedTimeMillis, now) } }
    }
    val todayGoodCount by remember(now) {
        derivedStateOf { deckCards.count { it.repetitions > 0 && it.lastStudiedTimeMillis > 0L && isSameDay(it.lastStudiedTimeMillis, now) } }
    }

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
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            if (sessionQueue.isEmpty()) {
                // Celebration Screen (BrainBob style)
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedUnicornCharacter(modifier = Modifier.fillMaxSize())
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        "Congratulations!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You finished this deck for now.",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "All due cards reviewed. Come back tomorrow!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Session Stats Card
                    Card(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                 Text("Studied", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                 Text("$todayStudiedCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                             }
                             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                 Text("Again (Forgot)", style = MaterialTheme.typography.bodyMedium, color = SrsColors.Again)
                                 Text("$todayAgainCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SrsColors.Again)
                             }
                             Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                 Text("Good / Easy", style = MaterialTheme.typography.bodyMedium, color = SrsColors.Good)
                                 Text("$todayGoodCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = SrsColors.Good)
                             }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(50.dp)
                        ) {
                            Text("Back to Home")
                        }
                        
                        if (allCards.any { it.deckId == deckId }) {
                            Button(
                                onClick = {
                                    val practiceCards = allCards.filter { it.deckId == deckId }
                                    sessionQueue.clear()
                                    sessionQueue.addAll(practiceCards.shuffled())
                                    studiedCount = 0
                                    againCount = 0
                                    goodCount = 0
                                },
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text("Practice Again")
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
                    animationSpec = tween(durationMillis = 400)
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
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.weight(1f).height(8.dp).graphicsLayer(shape = RoundedCornerShape(4.dp), clip = true),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "$remaining left",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
                        val wordColor = if (MaterialTheme.colorScheme.background.red > 0.5f) Color(0xFFD946EF) else Color(0xFFFB7185) // Fuchsia / coral pink
                        val cyanHighlight = Color(0xFF0ea5e9) // Vibrant cyan

                        if (rotation <= 90f) {
                            // FRONT SIDE
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { isFlipped = true },
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(24.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Top banner tag
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            "VOCABULARY",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(36.dp))

                                    // Word
                                    Text(
                                        currentCard.word,
                                        style = MaterialTheme.typography.headlineLarge.copy(fontSize = 40.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = wordColor,
                                        textAlign = TextAlign.Center
                                    )
                                    
                                    // Phonetic
                                    if (currentCard.phonetic.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            currentCard.phonetic,
                                            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                                            color = wordColor.copy(alpha = 0.7f),
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
                                        modifier = Modifier.size(64.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.PlayArrow,
                                            contentDescription = "Speak Word",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(36.dp)
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
                            // BACK SIDE (Rotated 180 degrees)
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer { rotationY = 180f },
                                shape = RoundedCornerShape(32.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(24.dp)
                                ) {
                                    // Header details
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                currentCard.word,
                                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = wordColor
                                            )
                                            if (currentCard.phonetic.isNotBlank()) {
                                                Text(
                                                    currentCard.phonetic,
                                                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 18.sp),
                                                    color = wordColor.copy(alpha = 0.7f),
                                                    fontStyle = FontStyle.Italic
                                                )
                                            }
                                        }
                                    }

                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                                    // Content Scroll Area
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        // Meaning (Cyan text)
                                        Column {
                                            Text(
                                                "Meaning",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                letterSpacing = 0.5.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Meaning: ${currentCard.meaning}",
                                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                                color = cyanHighlight
                                            )
                                        }

                                        // Example (white/black text, target word in cyan)
                                        if (currentCard.example.isNotBlank()) {
                                            Column {
                                                Text(
                                                    "Example",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                    letterSpacing = 0.5.sp
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                
                                                val annotatedExample = remember(currentCard.example, currentCard.word) {
                                                    buildAnnotatedString {
                                                        append("→ Example: ")
                                                        val text = currentCard.example
                                                        val word = currentCard.word
                                                        val index = text.indexOf(word, ignoreCase = true)
                                                        if (index >= 0) {
                                                            append(text.substring(0, index))
                                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = cyanHighlight)) {
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
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        
                                        Spacer(modifier = Modifier.weight(1f))

                                        // Three horizontal play buttons: Word, Sentence, Both
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                                        ) {
                                            // Word TTS
                                            AudioPlayButton(
                                                label = "Word",
                                                onClick = { TtsPlayer.speak(currentCard.word) }
                                            )
                                            // Sentence TTS
                                            if (currentCard.example.isNotBlank()) {
                                                AudioPlayButton(
                                                    label = "Sentence",
                                                    onClick = { TtsPlayer.speak(currentCard.example) }
                                                )
                                                // Sequenced TTS
                                                AudioPlayButton(
                                                    label = "All",
                                                    onClick = {
                                                        TtsPlayer.speak(currentCard.word)
                                                        // simple delay loop or sequential call trigger
                                                        TtsPlayer.speak(". ${currentCard.example}")
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }


                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    if (!isFlipped) {
                        Button(
                            onClick = { isFlipped = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)) // Slate grey banner
                        ) {
                            Text("Show Answer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else {
                        // Spaced Repetition Response Bar
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
                                    val reviewedCard = SrsEngine.review(currentCard, CardRating.AGAIN, now)
                                    val updatedCard = reviewedCard.copy(
                                        firstStudiedTimeMillis = if (currentCard.firstStudiedTimeMillis == 0L) now else currentCard.firstStudiedTimeMillis,
                                        lastStudiedTimeMillis = now
                                    )
                                    onSaveCard(updatedCard)
                                    sessionQueue.removeFirst()
                                    sessionQueue.add(updatedCard)
                                }
                            )

                            // Hard
                            val nextHard = SrsEngine.review(currentCard, CardRating.HARD, now)
                            val intervalHard = formatInterval(nextHard.intervalDays)
                            RatingButton(
                                title = "Hard",
                                interval = intervalHard,
                                color = SrsColors.Hard,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    studiedCount++
                                    val reviewedCard = SrsEngine.review(currentCard, CardRating.HARD, now)
                                    val updatedCard = reviewedCard.copy(
                                        firstStudiedTimeMillis = if (currentCard.firstStudiedTimeMillis == 0L) now else currentCard.firstStudiedTimeMillis,
                                        lastStudiedTimeMillis = now
                                    )
                                    onSaveCard(updatedCard)
                                    sessionQueue.removeFirst()
                                }
                            )

                            // Good
                            val nextGood = SrsEngine.review(currentCard, CardRating.GOOD, now)
                            val intervalGood = formatInterval(nextGood.intervalDays)
                            RatingButton(
                                title = "Good",
                                interval = intervalGood,
                                color = SrsColors.Good,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    studiedCount++
                                    goodCount++
                                    val reviewedCard = SrsEngine.review(currentCard, CardRating.GOOD, now)
                                    val updatedCard = reviewedCard.copy(
                                        firstStudiedTimeMillis = if (currentCard.firstStudiedTimeMillis == 0L) now else currentCard.firstStudiedTimeMillis,
                                        lastStudiedTimeMillis = now
                                    )
                                    onSaveCard(updatedCard)
                                    sessionQueue.removeFirst()
                                }
                            )

                            // Easy
                            val nextEasy = SrsEngine.review(currentCard, CardRating.EASY, now)
                            val intervalEasy = formatInterval(nextEasy.intervalDays)
                            RatingButton(
                                title = "Easy",
                                interval = intervalEasy,
                                color = SrsColors.Easy,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    studiedCount++
                                    goodCount++
                                    val reviewedCard = SrsEngine.review(currentCard, CardRating.EASY, now)
                                    val updatedCard = reviewedCard.copy(
                                        firstStudiedTimeMillis = if (currentCard.firstStudiedTimeMillis == 0L) now else currentCard.firstStudiedTimeMillis,
                                        lastStudiedTimeMillis = now
                                    )
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
fun AudioPlayButton(
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
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
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
            Text(interval, fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

fun formatInterval(days: Int): String {
    return when {
        days <= 0 -> "<1d"
        days < 7 -> "${days}d"
        days < 30 -> {
            val weeks = days / 7
            if (weeks == 0) "1w" else "${weeks}w"
        }
        else -> {
            val months = days / 30
            if (months == 0) "1m" else "${months}m"
        }
    }
}
