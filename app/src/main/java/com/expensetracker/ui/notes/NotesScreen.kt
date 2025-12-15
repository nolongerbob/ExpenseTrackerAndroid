package com.expensetracker.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.expensetracker.data.models.Note
import com.expensetracker.data.repository.NoteRepository
import com.expensetracker.data.local.PreferencesManager
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.ui.components.LiquidGlassCard
import com.expensetracker.ui.notes.EditNoteScreen
import com.expensetracker.ui.notes.NotesCalendarView
import com.expensetracker.ui.notes.AddNoteDialog
import com.expensetracker.ui.utils.AppColors
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    modifier: Modifier = Modifier,
    noteRepository: NoteRepository? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val retrofitClient = remember { RetrofitClient(preferencesManager) }
    val repository = noteRepository ?: remember { 
        NoteRepository(retrofitClient.apiService, retrofitClient) 
    }
    val viewModel: com.expensetracker.ui.viewmodel.NoteViewModel = viewModel(
        factory = com.expensetracker.ui.viewmodel.NoteViewModelFactory(repository)
    )
    
    val notes by viewModel.notes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showAddNote by remember { mutableStateOf(false) }
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var viewMode by remember { mutableStateOf(0) } // 0 = список, 1 = календарь
    var selectedDate by remember { mutableStateOf<Date?>(Date()) }
    
    // Обновляем данные при появлении экрана
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    
    val isLight = MaterialTheme.colorScheme.surface == Color.White
    val dateFormat = remember {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    }
    
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddNote = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить заметку")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.backgroundGradient(isLight))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Переключатель список/календарь
                    if (notes.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = viewMode == 0,
                                onClick = { viewMode = 0 },
                                label = { Text("Список") },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = viewMode == 1,
                                onClick = { viewMode = 1 },
                                label = { Text("Календарь") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    if (notes.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(androidx.compose.ui.Alignment.CenterHorizontally),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Нет заметок",
                                style = MaterialTheme.typography.titleLarge,
                                color = AppColors.secondaryText(isLight)
                            )
                        }
                    } else if (viewMode == 0) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = notes,
                                key = { it.id }
                            ) { note ->
                                NoteCard(
                                    note = note,
                                    dateFormat = dateFormat,
                                    onClick = { selectedNote = note },
                                    isLight = isLight
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            item {
                                NotesCalendarView(
                                    notes = notes,
                                    selectedDate = selectedDate,
                                    onDateSelected = { selectedDate = it },
                                    onNoteTap = { selectedNote = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    isLight = isLight
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    if (showAddNote) {
        AddNoteDialog(
            onDismiss = { showAddNote = false },
            onNoteAdded = {
                viewModel.refresh()
                showAddNote = false
            },
            noteRepository = repository,
            initialDate = selectedDate
        )
    }
    
    selectedNote?.let { note ->
        ModalBottomSheet(
            onDismissRequest = { selectedNote = null },
            containerColor = Color.Transparent
        ) {
            EditNoteScreen(
                note = note,
                noteRepository = repository,
                onNoteUpdated = {
                    viewModel.refresh()
                    selectedNote = null
                },
                onDismiss = { selectedNote = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteCard(
    note: Note,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit = {},
    isLight: Boolean = MaterialTheme.colorScheme.surface == Color.White
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        LiquidGlassCard(isLight = isLight) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.primaryText(isLight)
                    )
                    if (note.reminderDate != null) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFFFF9500),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                val contentText = note.content
                if (contentText != null && contentText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = contentText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.secondaryText(isLight)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dateFormat.format(Date(note.createdAt)),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.secondaryText(isLight)
                )
            }
        }
    }
}

