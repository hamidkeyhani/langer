package com.mizogy.langer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mizogy.langer.util.PlatformConvexVerifier
import com.mizogy.langer.util.TestAppState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvexTestScreen(
    taskIdParam: String? = null,
    onImportDeck: (String, String) -> Unit = { _, _ -> },
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var convexUrl by remember { mutableStateOf("https://ideal-peccary-626.convex.cloud") }
    var taskId by remember { mutableStateOf(taskIdParam ?: "test-task-123") }
    var verifier by remember { 
        mutableStateOf<PlatformConvexVerifier?>(
            if (taskIdParam != null) PlatformConvexVerifier("https://ideal-peccary-626.convex.cloud") else null
        )
    }
    var liveState by remember { mutableStateOf<TestAppState?>(null) }
    var statusMessage by remember { 
        mutableStateOf(
            if (taskIdParam != null) "Connecting to generation job..." else "Disconnected. Enter URL and connect."
        ) 
    }
    var updateCounter by remember { mutableStateOf(0) }
    var showImportDialog by remember { mutableStateOf(false) }

    // Subscribe to task updates when verifier is initialized
    LaunchedEffect(verifier, taskId) {
        verifier?.let { client ->
            statusMessage = "Subscribing to task $taskId..."
            try {
                client.subscribeToTask(taskId).collect { state ->
                    liveState = state
                    statusMessage = "Connected. Real-time updates active!"
                }
            } catch (e: Exception) {
                statusMessage = "Subscription Error: ${e.message}"
            }
        }
    }

    // Trigger popup dialog automatically when task is complete
    LaunchedEffect(liveState?.taskStatus, liveState?.generatedDeckId) {
        if (liveState?.generatedDeckId != null || liveState?.taskStatus == "Complete") {
            showImportDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Convex Real-Time Verifier", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Configuration
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("1. Connection Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    OutlinedTextField(
                        value = convexUrl,
                        onValueChange = { convexUrl = it },
                        label = { Text("Convex HTTP/WS URL") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = taskId,
                        onValueChange = { taskId = it },
                        label = { Text("Verification Task ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            if (convexUrl.isNotBlank() && convexUrl.startsWith("http")) {
                                verifier = PlatformConvexVerifier(convexUrl)
                            } else {
                                statusMessage = "Invalid URL. Please enter a valid Convex deployment URL."
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect and Subscribe")
                    }
                }
            }

            // Section 2: Real-time UI State (The core verification)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("2. Live State from Convex (Subscription)", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (liveState != null) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = "Status",
                            tint = if (liveState != null) Color(0xFF4CAF50) else Color.Gray
                        )
                        Text(
                            text = statusMessage,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (liveState != null) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text("Current Task Status: ${liveState?.taskStatus}", fontWeight = FontWeight.SemiBold)
                            
                            val rawMarkdown = liveState?.extractedMarkdown ?: "None"
                            val displayMarkdown = if (rawMarkdown.length > 300) {
                                rawMarkdown.take(300) + "\n\n[... content truncated for preview ...]"
                            } else {
                                rawMarkdown
                            }
                            
                            Text("Extracted Markdown:\n$displayMarkdown", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Devin Session ID: ${liveState?.devinSessionId ?: "None"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Devin Logs Counter: ${liveState?.devinLogs?.size ?: 0}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            

                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No live data yet. Click 'Connect and Subscribe' or trigger a mutation below.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Section 3: Trigger Dummy Payload (Mutation)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("3. Write Dummy Mutation", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Button(
                        onClick = {
                            verifier?.let { client ->
                                coroutineScope.launch {
                                    updateCounter++
                                    statusMessage = "Sending mutation payload..."
                                    try {
                                        client.updateTaskState(
                                            taskId = taskId,
                                            status = "Updated from Langer Compose App #$updateCounter",
                                            markdown = "This is a dummy scraped verification payload at run #$updateCounter",
                                            sessionId = "verifier-session-$updateCounter"
                                        )
                                        statusMessage = "Mutation succeeded! Waiting for reactive sync..."
                                    } catch (e: Exception) {
                                        statusMessage = "Mutation failed: ${e.message}"
                                    }
                                }
                            } ?: run {
                                statusMessage = "Error: Click 'Connect and Subscribe' first before running mutations."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Run")
                        Spacer(Modifier.width(8.dp))
                        Text("Trigger Mutation Payload")
                    }
                }
            }
        }
    }

    if (showImportDialog) {
        val generatedId = liveState?.generatedDeckId ?: "temp-deck-id"
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50)
                    )
                    Text("AI Generation Complete", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Your new vocabulary deck has been successfully created. Would you like to import it into your Langer decks?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showImportDialog = false
                        onImportDeck("AI Generated Vocab", generatedId)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Import Now", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
