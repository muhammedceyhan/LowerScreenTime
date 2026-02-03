package com.example.lowerscreentime.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lowerscreentime.data.Quote
import com.example.lowerscreentime.data.QuoteCommentDao
import com.example.lowerscreentime.data.QuoteDao
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    quoteId: Long,
    navController: NavController,
    quoteDao: QuoteDao,
    quoteCommentDao: QuoteCommentDao
) {
    val scope = rememberCoroutineScope()
    var quote by remember { mutableStateOf<Quote?>(null) }
    
    // Fetch quote - optimize to collect flow properly or just fetch once if needed, 
    // but collecting is better for updates if we edit.
    // Since quoteId is passed, we need a DAO method to get a single quote or filter from all.
    // For now, let's filter from all (simplest given current DAO) or add getById.
    // Adding getById is better practice but expensive context switch.
    // Let's rely on getAll and find for MVP or add getById to DAO quickly in next step if needed. 
    // Actually, `getAll` is already available.
    
    val allQuotes by quoteDao.getAll().collectAsState(initial = emptyList())
    
    LaunchedEffect(allQuotes, quoteId) {
        quote = allQuotes.find { it.id == quoteId }
    }

    val comments by quoteCommentDao.getCommentsForQuote(quoteId).collectAsState(initial = emptyList())
    
    var showEditDialog by remember { mutableStateOf(false) }
    var editContent by remember { mutableStateOf("") }

    if (quote != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Quote Details") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            editContent = quote!!.content
                            showEditDialog = true 
                        }) {
                            Icon(Icons.Default.Edit, "Edit Quote")
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Quote Display
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = quote!!.content,
                            style = MaterialTheme.typography.headlineMedium,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(quote!!.timestamp)),
                            style = MaterialTheme.typography.labelMedium,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }

                // Comments Section
                Text(
                    "Reflections",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
                )
                
                if (comments.isEmpty()) {
                    Text(
                        "No reflections yet. Use the Focus Timer to add one!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f)
                    ) {
                        items(comments) { comment ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(comment.text, style = MaterialTheme.typography.bodyLarge)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        SimpleDateFormat("HH:mm - MMM dd", Locale.getDefault()).format(Date(comment.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            if (showEditDialog) {
                AlertDialog(
                    onDismissRequest = { showEditDialog = false },
                    title = { Text("Edit Quote") },
                    text = {
                        OutlinedTextField(
                            value = editContent,
                            onValueChange = { editContent = it },
                            label = { Text("Content") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (editContent.isNotBlank()) {
                                scope.launch {
                                    quoteDao.update(quote!!.copy(content = editContent))
                                    showEditDialog = false
                                }
                            }
                        }) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    } else {
        // Loading or not found
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
