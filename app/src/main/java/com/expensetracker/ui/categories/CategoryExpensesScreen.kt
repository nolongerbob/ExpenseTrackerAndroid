package com.expensetracker.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.expensetracker.data.models.Category
import com.expensetracker.data.models.Expense
import com.expensetracker.data.repository.ExpenseRepository
import com.expensetracker.data.local.PreferencesManager
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.ui.components.LiquidGlassCard
import com.expensetracker.ui.utils.AppColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExpensesScreen(
    category: Category,
    modifier: Modifier = Modifier,
    expenseRepository: ExpenseRepository? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val retrofitClient = remember { RetrofitClient(preferencesManager) }
    val repository = expenseRepository ?: remember { 
        ExpenseRepository(retrofitClient.apiService, retrofitClient) 
    }
    
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    
    val isLight = MaterialTheme.colorScheme.surface == Color.White
    
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply {
            currency = Currency.getInstance("RUB")
            maximumFractionDigits = 0
        }
    }
    
    val dateFormat = remember {
        SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    }
    
    LaunchedEffect(category.id) {
        repository.getExpenses()
            .onSuccess { allExpenses ->
                expenses = allExpenses
                    .filter { it.category.id == category.id }
                    .sortedByDescending { it.date }
                isLoading = false
            }
            .onFailure {
                isLoading = false
            }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(category.name) }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AppColors.backgroundGradient(isLight))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            } else if (expenses.isEmpty()) {
                Column(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Нет расходов в этой категории",
                        style = MaterialTheme.typography.titleLarge,
                        color = AppColors.secondaryText(isLight)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        items = expenses,
                        key = { it.id.toString() }
                    ) { expense ->
                        ExpenseRow(
                            expense = expense,
                            currencyFormat = currencyFormat,
                            dateFormat = dateFormat,
                            isLight = isLight
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseRow(
    expense: Expense,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    isLight: Boolean = MaterialTheme.colorScheme.surface == Color.White
) {
    LiquidGlassCard(isLight = isLight) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val noteText = expense.note
                if (noteText != null && noteText.isNotBlank()) {
                    Text(
                        text = noteText,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.primaryText(isLight)
                    )
                }
                Text(
                    text = dateFormat.format(Date(expense.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.secondaryText(isLight)
                )
            }
            Text(
                text = if (expense.type == Expense.ExpenseType.EXPENSE) {
                    "-${currencyFormat.format(expense.amount)}"
                } else {
                    "+${currencyFormat.format(expense.amount)}"
                },
                style = MaterialTheme.typography.titleLarge,
                color = if (expense.type == Expense.ExpenseType.EXPENSE) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        }
    }
}

