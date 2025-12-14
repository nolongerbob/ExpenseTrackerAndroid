package com.expensetracker.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class Expense(
    val id: UUID = UUID.randomUUID(),
    val apiId: String,
    var amount: Double,
    var category: Category,
    var note: String? = null,
    var date: Long, // Timestamp
    var type: ExpenseType = ExpenseType.EXPENSE
) : Parcelable {
    enum class ExpenseType {
        EXPENSE, INCOME;
        
        companion object {
            fun fromAPI(apiType: String?): ExpenseType {
                return if (apiType == "INCOME") INCOME else EXPENSE
            }
        }
    }
}

