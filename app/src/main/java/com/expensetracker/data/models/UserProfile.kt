package com.expensetracker.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserProfile(
    val id: String,
    var name: String,
    var email: String,
    var avatar: String? = null
) : Parcelable

