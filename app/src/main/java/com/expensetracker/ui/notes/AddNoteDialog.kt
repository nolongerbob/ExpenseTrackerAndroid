package com.expensetracker.ui.notes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.expensetracker.data.repository.NoteRepository
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onNoteAdded: () -> Unit,
    noteRepository: NoteRepository,
    initialDate: Date? = null
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var hasNoteDate by remember { mutableStateOf(initialDate != null) }
    var noteDate by remember { mutableStateOf(initialDate ?: Date()) }
    var hasReminder by remember { mutableStateOf(false) }
    var reminderDate by remember { mutableStateOf(Date()) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая заметка") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Заголовок") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Содержание") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 5
                )
                
                // Дата заметки
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Дата заметки",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = hasNoteDate,
                        onCheckedChange = { hasNoteDate = it }
                    )
                }
                if (hasNoteDate) {
                    Text(
                        text = "Дата: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(noteDate)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // Напоминание
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Напоминание",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(
                        checked = hasReminder,
                        onCheckedChange = { hasReminder = it }
                    )
                }
                if (hasReminder) {
                    Text(
                        text = "Напоминание: ${SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(reminderDate)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        isLoading = true
                        coroutineScope.launch {
                            noteRepository.createNote(
                                title = title,
                                content = content.ifBlank { null },
                                noteDate = if (hasNoteDate) noteDate.time else null,
                                reminderDate = if (hasReminder) reminderDate.time else null
                            ).onSuccess {
                                isLoading = false
                                onNoteAdded()
                            }.onFailure {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading && title.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Создать")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}


