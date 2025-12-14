package com.expensetracker.data.models

import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val id: String,
    var name: String,
    var colorHex: String,
    var icon: String,
    var type: Expense.ExpenseType
) : Parcelable {
    fun getColor(): Color {
        return Color(android.graphics.Color.parseColor(colorHex))
    }
    
    companion object {
        fun colorToHex(color: Color): String {
            val colorInt = color.value.toInt()
            return String.format("#%06X", 0xFFFFFF and colorInt)
        }
    }
}

