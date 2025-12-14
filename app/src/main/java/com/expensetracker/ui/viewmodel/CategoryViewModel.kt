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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryTotal(
    val category: Category,
    val total: Double,
    val count: Int,
    val percentage: Double
)

class CategoryViewModel(
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
    
    val categoryTotals: StateFlow<List<CategoryTotal>> = _expenses.map { expenses ->
        val expenseExpenses = expenses.filter { it.type == Expense.ExpenseType.EXPENSE }
        val totalSum = expenseExpenses.sumOf { it.amount }
        
        _categories.value.map { category ->
            val categoryExpenses = expenseExpenses.filter { it.category.id == category.id }
            val total = categoryExpenses.sumOf { it.amount }
            val percentage = if (totalSum > 0) (total / totalSum) * 100 else 0.0
            
            CategoryTotal(
                category = category,
                total = total,
                count = categoryExpenses.size,
                percentage = percentage
            )
        }.sortedByDescending { it.total }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            expenseRepository.getExpenses()
                .onSuccess { expenses ->
                    _expenses.value = expenses
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            categoryRepository.getCategories()
                .onSuccess { categories ->
                    _categories.value = categories
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun refresh() {
        loadData()
    }
}

