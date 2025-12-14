package com.expensetracker.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val id: String,
    var title: String,
    var content: String? = null,
    var noteDate: Long, // Timestamp
    var reminderDate: Long? = null, // Timestamp
    var createdAt: Long, // Timestamp
    var updatedAt: Long // Timestamp
) : Parcelable

