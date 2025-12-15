package com.expensetracker.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.expensetracker.data.models.Note
import com.expensetracker.data.repository.NoteRepository
import com.expensetracker.data.local.PreferencesManager
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.ui.components.LiquidGlassCard

@Composable
fun EditNoteScreen(
    note: Note,
    modifier: Modifier = Modifier,
    noteRepository: NoteRepository? = null,
    onNoteUpdated: () -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val retrofitClient = remember { RetrofitClient(preferencesManager) }
    val repository = noteRepository ?: remember { 
        NoteRepository(retrofitClient.apiService, retrofitClient) 
    }
    
    var title by remember { mutableStateOf(note.title) }
    var content by remember { mutableStateOf(note.content ?: "") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            LiquidGlassCard {
                Column {
                    Text(
                        text = "Заголовок",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Заголовок") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            LiquidGlassCard {
                Column {
                    Text(
                        text = "Содержание",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Содержание") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 5,
                        maxLines = 10
                    )
                }
            }
            
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        isLoading = true
                        coroutineScope.launch {
                            repository.updateNote(
                                id = note.id,
                                title = title,
                                content = content.ifBlank { null },
                                noteDate = null,
                                reminderDate = null
                            ).onSuccess {
                                isLoading = false
                                onNoteUpdated()
                            }.onFailure {
                                isLoading = false
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && title.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Сохранить изменения")
                }
            }
            
            Button(
                onClick = {
                    coroutineScope.launch {
                        repository.deleteNote(note.id)
                            .onSuccess {
                                onNoteUpdated()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                enabled = !isLoading
            ) {
                Text("Удалить")
            }
            
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Отмена")
            }
        }
    }
}




