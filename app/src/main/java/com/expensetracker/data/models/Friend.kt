package com.expensetracker.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Friend(
    val id: String,
    var name: String,
    var email: String,
    var avatar: String? = null
) : Parcelable

