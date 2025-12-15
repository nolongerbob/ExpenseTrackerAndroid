package com.expensetracker.data.repository

import com.expensetracker.data.models.Note
import com.expensetracker.data.remote.ApiService
import com.expensetracker.data.remote.CreateNoteRequest
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.data.remote.UpdateNoteRequest
import java.text.SimpleDateFormat
import java.util.*

class NoteRepository(
    private val apiService: ApiService,
    private val retrofitClient: RetrofitClient
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    suspend fun getNotes(): Result<List<Note>> {
        return try {
            val response = apiService.getNotes()
            if (response.isSuccessful && response.body() != null) {
                val notes = response.body()!!.map { noteResponse ->
                    Note(
                        id = noteResponse.id,
                        title = noteResponse.title,
                        content = noteResponse.content,
                        noteDate = parseDate(noteResponse.noteDate) ?: System.currentTimeMillis(),
                        reminderDate = noteResponse.reminderDate?.let { parseDate(it) },
                        createdAt = parseDate(noteResponse.createdAt) ?: System.currentTimeMillis(),
                        updatedAt = parseDate(noteResponse.updatedAt) ?: System.currentTimeMillis()
                    )
                }
                Result.success(notes)
            } else {
                Result.failure(Exception("Failed to fetch notes: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createNote(
        title: String,
        content: String?,
        noteDate: Long?,
        reminderDate: Long?
    ): Result<Note> {
        return try {
            val request = CreateNoteRequest(
                title = title,
                content = content,
                noteDate = noteDate?.let { formatDate(it) },
                reminderDate = reminderDate?.let { formatDate(it) }
            )
            val response = apiService.createNote(request)
            if (response.isSuccessful && response.body() != null) {
                val noteResponse = response.body()!!
                val note = Note(
                    id = noteResponse.id,
                    title = noteResponse.title,
                    content = noteResponse.content,
                    noteDate = parseDate(noteResponse.noteDate) ?: System.currentTimeMillis(),
                    reminderDate = noteResponse.reminderDate?.let { parseDate(it) },
                    createdAt = parseDate(noteResponse.createdAt) ?: System.currentTimeMillis(),
                    updatedAt = parseDate(noteResponse.updatedAt) ?: System.currentTimeMillis()
                )
                Result.success(note)
            } else {
                Result.failure(Exception("Failed to create note: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateNote(
        id: String,
        title: String?,
        content: String?,
        noteDate: Long?,
        reminderDate: Long?
    ): Result<Note> {
        return try {
            val request = UpdateNoteRequest(
                title = title,
                content = content,
                noteDate = noteDate?.let { formatDate(it) },
                reminderDate = reminderDate?.let { formatDate(it) }
            )
            val response = apiService.updateNote(id, request)
            if (response.isSuccessful && response.body() != null) {
                val noteResponse = response.body()!!
                val note = Note(
                    id = noteResponse.id,
                    title = noteResponse.title,
                    content = noteResponse.content,
                    noteDate = parseDate(noteResponse.noteDate) ?: System.currentTimeMillis(),
                    reminderDate = noteResponse.reminderDate?.let { parseDate(it) },
                    createdAt = parseDate(noteResponse.createdAt) ?: System.currentTimeMillis(),
                    updatedAt = parseDate(noteResponse.updatedAt) ?: System.currentTimeMillis()
                )
                Result.success(note)
            } else {
                Result.failure(Exception("Failed to update note: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteNote(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteNote(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete note: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun parseDate(dateString: String): Long? {
        return try {
            dateFormat.parse(dateString)?.time
        } catch (e: Exception) {
            null
        }
    }
    
    private fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }
}




