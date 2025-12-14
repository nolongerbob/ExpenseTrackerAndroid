package com.expensetracker.data.repository

import com.expensetracker.data.models.Category
import com.expensetracker.data.models.Expense
import com.expensetracker.data.remote.ApiService
import com.expensetracker.data.remote.CreateExpenseRequest
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.data.remote.UpdateExpenseRequest
import java.text.SimpleDateFormat
import java.util.*

class ExpenseRepository(
    private val apiService: ApiService,
    private val retrofitClient: RetrofitClient
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    
    suspend fun getExpenses(): Result<List<Expense>> {
        return try {
            android.util.Log.d("ExpenseRepository", "Fetching expenses from API...")
            val response = apiService.getExpenses()
            android.util.Log.d("ExpenseRepository", "Response code: ${response.code()}, isSuccessful: ${response.isSuccessful}")
            if (response.isSuccessful && response.body() != null) {
                android.util.Log.d("ExpenseRepository", "Response body size: ${response.body()!!.size}")
                val expenses = response.body()!!.map { expenseResponse ->
                    Expense(
                        id = safeParseUUID(expenseResponse.id),
                        apiId = expenseResponse.id,
                        amount = expenseResponse.amount.toDoubleOrNull() ?: 0.0,
                        category = expenseResponse.category?.let { cat ->
                            Category(
                                id = cat.id,
                                name = cat.name,
                                colorHex = cat.color,
                                icon = "tag.fill",
                                type = Expense.ExpenseType.fromAPI(cat.type)
                            )
                        } ?: Category(
                            id = "none",
                            name = "Без категории",
                            colorHex = "#808080",
                            icon = "tag.fill",
                            type = Expense.ExpenseType.fromAPI(expenseResponse.type)
                        ),
                        note = expenseResponse.note,
                        date = parseDate(expenseResponse.spentAt) ?: System.currentTimeMillis(),
                        type = Expense.ExpenseType.fromAPI(expenseResponse.type)
                    )
                }
                Result.success(expenses)
            } else {
                Result.failure(Exception("Failed to fetch expenses: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createExpense(
        amount: Double,
        categoryId: String?,
        note: String?,
        type: String = "EXPENSE"
    ): Result<Expense> {
        return try {
            val request = CreateExpenseRequest(
                amount = amount,
                categoryId = categoryId,
                note = note,
                type = type
            )
            val response = apiService.createExpense(request)
            if (response.isSuccessful && response.body() != null) {
                val expenseResponse = response.body()!!
                val expense = Expense(
                    id = safeParseUUID(expenseResponse.id),
                    apiId = expenseResponse.id,
                    amount = expenseResponse.amount.toDoubleOrNull() ?: 0.0,
                    category = expenseResponse.category?.let { cat ->
                        Category(
                            id = cat.id,
                            name = cat.name,
                            colorHex = cat.color,
                            icon = "tag.fill",
                            type = Expense.ExpenseType.fromAPI(cat.type)
                        )
                    } ?: Category(
                        id = "none",
                        name = "Без категории",
                        colorHex = "#808080",
                        icon = "tag.fill",
                        type = Expense.ExpenseType.fromAPI(expenseResponse.type)
                    ),
                    note = expenseResponse.note,
                    date = parseDate(expenseResponse.spentAt) ?: System.currentTimeMillis(),
                    type = Expense.ExpenseType.fromAPI(expenseResponse.type)
                )
                Result.success(expense)
            } else {
                Result.failure(Exception("Failed to create expense: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateExpense(
        id: String,
        amount: Double?,
        categoryId: String?,
        note: String?,
        type: String?
    ): Result<Expense> {
        return try {
            val request = UpdateExpenseRequest(
                amount = amount,
                categoryId = categoryId,
                note = note,
                type = type
            )
            val response = apiService.updateExpense(id, request)
            if (response.isSuccessful && response.body() != null) {
                val expenseResponse = response.body()!!
                val expense = Expense(
                    id = safeParseUUID(expenseResponse.id),
                    apiId = expenseResponse.id,
                    amount = expenseResponse.amount.toDoubleOrNull() ?: 0.0,
                    category = expenseResponse.category?.let { cat ->
                        Category(
                            id = cat.id,
                            name = cat.name,
                            colorHex = cat.color,
                            icon = "tag.fill",
                            type = Expense.ExpenseType.fromAPI(cat.type)
                        )
                    } ?: Category(
                        id = "none",
                        name = "Без категории",
                        colorHex = "#808080",
                        icon = "tag.fill",
                        type = Expense.ExpenseType.fromAPI(expenseResponse.type)
                    ),
                    note = expenseResponse.note,
                    date = parseDate(expenseResponse.spentAt) ?: System.currentTimeMillis(),
                    type = Expense.ExpenseType.fromAPI(expenseResponse.type)
                )
                Result.success(expense)
            } else {
                Result.failure(Exception("Failed to update expense: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteExpense(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteExpense(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete expense: ${response.code()}"))
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
    
    private fun safeParseUUID(id: String): UUID {
        return try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            // Если ID не является валидным UUID, создаем детерминированный UUID из строки
            android.util.Log.w("ExpenseRepository", "Invalid UUID format: $id, generating deterministic UUID")
            UUID.nameUUIDFromBytes(id.toByteArray())
        }
    }
}

