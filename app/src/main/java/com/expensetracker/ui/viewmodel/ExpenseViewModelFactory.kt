package com.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.repository.ExpenseRepository

class ExpenseViewModelFactory(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            val catRepo = categoryRepository ?: throw IllegalArgumentException("CategoryRepository is required")
            return ExpenseViewModel(expenseRepository, catRepo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}




