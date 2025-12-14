package com.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.models.Category
import com.expensetracker.data.models.Expense
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.repository.ExpenseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()
    
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadExpenses()
        loadCategories()
    }
    
    fun loadExpenses() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            expenseRepository.getExpenses()
                .onSuccess { expenses ->
                    _expenses.value = expenses.sortedByDescending { it.date }
                    android.util.Log.d("ExpenseViewModel", "Loaded ${expenses.size} expenses")
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    android.util.Log.e("ExpenseViewModel", "Failed to load expenses", exception)
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getCategories()
                .onSuccess { categories ->
                    _categories.value = categories
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
    
    fun createExpense(
        amount: Double,
        categoryId: String?,
        note: String?,
        type: String = "EXPENSE"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            expenseRepository.createExpense(amount, categoryId, note, type)
                .onSuccess { expense ->
                    _expenses.value = (listOf(expense) + _expenses.value).sortedByDescending { it.date }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun updateExpense(
        id: String,
        amount: Double?,
        categoryId: String?,
        note: String?,
        type: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            expenseRepository.updateExpense(id, amount, categoryId, note, type)
                .onSuccess { expense ->
                    _expenses.value = _expenses.value.map {
                        if (it.apiId == id) expense else it
                    }.sortedByDescending { it.date }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun deleteExpense(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            expenseRepository.deleteExpense(id)
                .onSuccess {
                    _expenses.value = _expenses.value.filter { it.apiId != id }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun createCategory(name: String, color: String, type: String = "EXPENSE") {
        viewModelScope.launch {
            categoryRepository.createCategory(name, color, type)
                .onSuccess { category ->
                    _categories.value = _categories.value + category
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
        }
    }
    
    fun refresh() {
        loadExpenses()
        loadCategories()
    }
}
