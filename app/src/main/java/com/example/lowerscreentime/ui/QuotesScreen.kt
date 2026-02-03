package com.example.lowerscreentime.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.lowerscreentime.data.QuoteDao
import com.example.lowerscreentime.data.Quote
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(
    navController: NavController,
    quoteDao: QuoteDao
) {
    val quotes by quoteDao.getAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var quoteToEdit by remember { mutableStateOf<Quote?>(null) }
    var contentInput by remember { mutableStateOf("") }
    
    // Open edit dialog
    fun openEdit(quote: Quote) {
        quoteToEdit = quote
        contentInput = quote.content
    }

    // Open add dialog
    fun openAdd() {
        quoteToEdit = null
        contentInput = ""
        showAddDialog = true
    }

    // Move Up
    fun moveUp(index: Int) {
        if (index > 0) {
            val current = quotes[index]
            val previous = quotes[index - 1]
            scope.launch {
                // Swap indices
                quoteDao.update(current.copy(orderIndex = previous.orderIndex))
                quoteDao.update(previous.copy(orderIndex = current.orderIndex))
            }
        }
    }

    // Move Down
    fun moveDown(index: Int) {
        if (index < quotes.size - 1) {
            val current = quotes[index]
            val next = quotes[index + 1]
            scope.launch {
                quoteDao.update(current.copy(orderIndex = next.orderIndex))
                quoteDao.update(next.copy(orderIndex = current.orderIndex))
            }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { openAdd() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Quote")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                "Quotes",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (quotes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No quotes added yet.", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quotes.size, key = { quotes[it].id }) { index ->
                        val quote = quotes[index]
                        Card(
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth().clickable { 
                                navController.navigate("quote_detail/${quote.id}") 
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(quote.content, style = MaterialTheme.typography.headlineSmall, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(quote.timestamp)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.Gray
                                    )
                                }
                                
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Up/Down Buttons
                                    Column {
                                        IconButton(onClick = { moveUp(index) }, enabled = index > 0, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.KeyboardArrowUp, "Up")
                                        }
                                        IconButton(onClick = { moveDown(index) }, enabled = index < quotes.size - 1, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.KeyboardArrowDown, "Down")
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(onClick = {
                                        scope.launch {
                                            quoteDao.delete(quote)
                                        }
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog || quoteToEdit != null) {
            AlertDialog(
                onDismissRequest = { 
                    showAddDialog = false 
                    quoteToEdit = null
                },
                title = { Text(if (quoteToEdit == null) "Add Quote" else "Edit Quote") },
                text = {
                    OutlinedTextField(
                        value = contentInput,
                        onValueChange = { contentInput = it },
                        label = { Text("Quote") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (contentInput.isNotBlank()) {
                            scope.launch {
                                if (quoteToEdit == null) {
                                    // Add new - calculate new index based on list size or manual?
                                    // Simple logic: add to end (size)
                                    val newIndex = quotes.size
                                    quoteDao.insert(Quote(content = contentInput, orderIndex = newIndex))
                                } else {
                                    // Update existing
                                    quoteDao.update(quoteToEdit!!.copy(content = contentInput))
                                }
                                contentInput = ""
                                showAddDialog = false
                                quoteToEdit = null
                            }
                        }
                    }) {
                        Text(if (quoteToEdit == null) "Add" else "Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showAddDialog = false 
                        quoteToEdit = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
