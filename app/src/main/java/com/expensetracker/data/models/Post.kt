package com.expensetracker.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Post(
    val id: UUID = UUID.randomUUID(),
    val apiId: String,
    var content: String,
    var author: UserProfile,
    var authorId: String,
    var createdAt: Long, // Timestamp
    var imageUrl: String? = null,
    var likes: List<Like> = emptyList(),
    var comments: List<Comment> = emptyList()
) : Parcelable

@Parcelize
data class Like(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID
) : Parcelable

@Parcelize
data class Comment(
    val id: UUID = UUID.randomUUID(),
    val apiId: String,
    var content: String,
    var author: UserProfile,
    var authorId: String,
    var createdAt: Long = System.currentTimeMillis()
) : Parcelable

