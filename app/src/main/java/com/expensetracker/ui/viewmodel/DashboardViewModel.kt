package com.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.expensetracker.data.models.Category
import com.expensetracker.data.models.Expense
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.repository.ExpenseRepository
import com.expensetracker.ui.components.PieChartData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.*

class DashboardViewModel(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    val currentMonthExpenses: StateFlow<List<Expense>> = _expenses.map { expenses ->
        val calendar = Calendar.getInstance()
        val now = Date()
        calendar.time = now
        val startOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val endOfMonth = calendar.apply {
            add(Calendar.MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        
        expenses.filter { expense ->
            expense.type == Expense.ExpenseType.EXPENSE &&
            expense.date >= startOfMonth &&
            expense.date <= endOfMonth
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val currentMonthIncome: StateFlow<List<Expense>> = _expenses.map { expenses ->
        val calendar = Calendar.getInstance()
        val now = Date()
        calendar.time = now
        val startOfMonth = calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val endOfMonth = calendar.apply {
            add(Calendar.MONTH, 1)
            add(Calendar.DAY_OF_MONTH, -1)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        
        expenses.filter { expense ->
            expense.type == Expense.ExpenseType.INCOME &&
            expense.date >= startOfMonth &&
            expense.date <= endOfMonth
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    val totalExpenses: StateFlow<Double> = currentMonthExpenses.map { expenses ->
        expenses.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    val totalIncome: StateFlow<Double> = currentMonthIncome.map { income ->
        income.sumOf { it.amount }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
    
    val categoryChartData: StateFlow<List<PieChartData>> = combine(
        currentMonthExpenses,
        _categories
    ) { expenses, categories ->
        val totals = expenses.groupBy { it.category.id }
            .mapValues { (_, expenseList) -> expenseList.sumOf { it.amount } }
        
        val totalSum = totals.values.sum()
        
        totals.mapNotNull { (categoryId, total) ->
            val category = categories.firstOrNull { it.id == categoryId } ?: return@mapNotNull null
            val percentage = if (totalSum > 0) (total / totalSum) * 100 else 0.0
            PieChartData(category, total, percentage)
        }.sortedByDescending { it.total }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
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
                    _expenses.value = expenses
                    android.util.Log.d("DashboardViewModel", "Loaded ${expenses.size} expenses")
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    android.util.Log.e("DashboardViewModel", "Failed to load expenses", exception)
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
    
    fun refresh() {
        loadExpenses()
        loadCategories()
    }
}

