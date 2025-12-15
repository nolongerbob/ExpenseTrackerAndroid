package com.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.models.Note
import com.expensetracker.data.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NoteViewModel(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadNotes()
    }
    
    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            noteRepository.getNotes()
                .onSuccess { notes ->
                    _notes.value = notes.sortedByDescending { it.createdAt }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun createNote(
        title: String,
        content: String?,
        noteDate: Long?,
        reminderDate: Long?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            noteRepository.createNote(title, content, noteDate, reminderDate)
                .onSuccess { note ->
                    _notes.value = (listOf(note) + _notes.value).sortedByDescending { it.createdAt }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun updateNote(
        id: String,
        title: String?,
        content: String?,
        noteDate: Long?,
        reminderDate: Long?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            noteRepository.updateNote(id, title, content, noteDate, reminderDate)
                .onSuccess { note ->
                    _notes.value = _notes.value.map {
                        if (it.id == id) note else it
                    }.sortedByDescending { it.createdAt }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun deleteNote(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            noteRepository.deleteNote(id)
                .onSuccess {
                    _notes.value = _notes.value.filter { it.id != id }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun refresh() {
        loadNotes()
    }
}




