package com.expensetracker.data.repository

import com.expensetracker.data.models.Category
import com.expensetracker.data.models.Expense
import com.expensetracker.data.remote.ApiService
import com.expensetracker.data.remote.CreateCategoryRequest
import com.expensetracker.data.remote.RetrofitClient

class CategoryRepository(
    private val apiService: ApiService,
    private val retrofitClient: RetrofitClient
) {
    suspend fun getCategories(type: String? = null): Result<List<Category>> {
        return try {
            val response = apiService.getCategories(type)
            if (response.isSuccessful && response.body() != null) {
                val categories = response.body()!!.map { cat ->
                    Category(
                        id = cat.id,
                        name = cat.name,
                        colorHex = cat.color,
                        icon = "tag.fill",
                        type = Expense.ExpenseType.fromAPI(cat.type)
                    )
                }
                Result.success(categories)
            } else {
                Result.failure(Exception("Failed to fetch categories: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createCategory(name: String, color: String, type: String = "EXPENSE"): Result<Category> {
        return try {
            val request = CreateCategoryRequest(name, color, type)
            val response = apiService.createCategory(request)
            if (response.isSuccessful && response.body() != null) {
                val cat = response.body()!!
                val category = Category(
                    id = cat.id,
                    name = cat.name,
                    colorHex = cat.color,
                    icon = "tag.fill",
                    type = Expense.ExpenseType.fromAPI(cat.type)
                )
                Result.success(category)
            } else {
                Result.failure(Exception("Failed to create category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCategory(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteCategory(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete category: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}




