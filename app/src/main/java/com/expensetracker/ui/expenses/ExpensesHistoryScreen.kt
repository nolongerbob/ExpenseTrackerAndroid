package com.expensetracker.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.data.models.Expense
import com.expensetracker.data.models.Category
import com.expensetracker.data.repository.ExpenseRepository
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.local.PreferencesManager
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.ui.components.LiquidGlassCard
import com.expensetracker.ui.components.PieChartView
import com.expensetracker.ui.components.PieChartData
import com.expensetracker.ui.expenses.EditExpenseScreen
import com.expensetracker.ui.utils.AppColors
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

enum class TransactionType {
    EXPENSES, INCOME
}

enum class PeriodFilter(val displayName: String) {
    ALL("Все время"),
    TODAY("Сегодня"),
    WEEK("Неделя"),
    MONTH("Месяц"),
    YEAR("Год"),
    CUSTOM("Выбрать даты")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesHistoryScreen(
    modifier: Modifier = Modifier,
    expenseRepository: ExpenseRepository? = null,
    categoryRepository: CategoryRepository? = null,
    transactionType: TransactionType = TransactionType.EXPENSES
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val retrofitClient = remember { RetrofitClient(preferencesManager) }
    val repository = expenseRepository ?: remember { 
        ExpenseRepository(retrofitClient.apiService, retrofitClient) 
    }
    val catRepo = categoryRepository ?: remember {
        CategoryRepository(retrofitClient.apiService, retrofitClient)
    }
    val viewModel: com.expensetracker.ui.viewmodel.ExpenseViewModel = viewModel(
        factory = com.expensetracker.ui.viewmodel.ExpenseViewModelFactory(repository, catRepo)
    )
    
    val expenses by viewModel.expenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedExpense by remember { mutableStateOf<Expense?>(null) }
    var selectedTransactionType by remember { mutableStateOf(transactionType) }
    var selectedPeriod by remember { mutableStateOf(PeriodFilter.MONTH) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    var startDate by remember { mutableStateOf(calendar.time) }
    var endDate by remember { mutableStateOf(Date()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    
    // Обновляем данные при появлении экрана
    LaunchedEffect(Unit) {
        if (expenses.isEmpty() && !isLoading) {
            viewModel.refresh()
        }
    }
    
    val isLight = MaterialTheme.colorScheme.surface == Color.White
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply {
            currency = Currency.getInstance("RUB")
            maximumFractionDigits = 0
        }
    }
    
    // Фильтрация расходов
    val filteredExpenses = remember(expenses, selectedTransactionType, selectedPeriod, selectedCategory, startDate, endDate) {
        var filtered = expenses
        
        // Фильтр по типу транзакции
        val targetType = if (selectedTransactionType == TransactionType.EXPENSES) {
            Expense.ExpenseType.EXPENSE
        } else {
            Expense.ExpenseType.INCOME
        }
        filtered = filtered.filter { it.type == targetType }
        
        // Фильтр по категории
        if (selectedCategory != null) {
            filtered = filtered.filter { it.category.id == selectedCategory!!.id }
        }
        
        // Фильтр по периоду
        val calendar = Calendar.getInstance()
        val now = Date()
        
        when (selectedPeriod) {
            PeriodFilter.ALL -> { /* Нет фильтра */ }
            PeriodFilter.TODAY -> {
                calendar.time = now
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                filtered = filtered.filter { it.date >= start && it.date <= end }
            }
            PeriodFilter.WEEK -> {
                calendar.time = now
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.timeInMillis
                filtered = filtered.filter { it.date >= weekAgo }
            }
            PeriodFilter.MONTH -> {
                calendar.time = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.add(Calendar.MONTH, 1)
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                filtered = filtered.filter { it.date >= start && it.date <= end }
            }
            PeriodFilter.YEAR -> {
                calendar.time = now
                calendar.add(Calendar.YEAR, -1)
                val yearAgo = calendar.timeInMillis
                filtered = filtered.filter { it.date >= yearAgo }
            }
            PeriodFilter.CUSTOM -> {
                val start = startDate.time
                val end = endDate.time
                filtered = filtered.filter { it.date >= start && it.date <= end }
            }
        }
        
        filtered.sortedByDescending { it.date }
    }
    
    val totalFiltered = filteredExpenses.sumOf { it.amount }
    
    // Данные для диаграммы по категориям (для расходов)
    val categoryChartData = remember(filteredExpenses, selectedTransactionType) {
        val expenseExpenses = filteredExpenses.filter { it.type == Expense.ExpenseType.EXPENSE }
        val totalSum = expenseExpenses.sumOf { it.amount }
        
        if (totalSum > 0 && selectedTransactionType == TransactionType.EXPENSES) {
            categories.map { category ->
                val categoryExpenses = expenseExpenses.filter { it.category.id == category.id }
                val total = categoryExpenses.sumOf { it.amount }
                val percentage = (total / totalSum) * 100
                PieChartData(
                    category = category,
                    total = total,
                    percentage = percentage
                )
            }.filter { it.total > 0 }.sortedByDescending { it.total }
        } else {
            emptyList()
        }
    }
    
    // Данные для диаграммы по категориям (для доходов)
    val incomeChartData = remember(filteredExpenses, selectedTransactionType) {
        val incomeExpenses = filteredExpenses.filter { it.type == Expense.ExpenseType.INCOME }
        val totalSum = incomeExpenses.sumOf { it.amount }
        
        if (totalSum > 0 && selectedTransactionType == TransactionType.INCOME) {
            categories.map { category ->
                val categoryExpenses = incomeExpenses.filter { it.category.id == category.id }
                val total = categoryExpenses.sumOf { it.amount }
                val percentage = (total / totalSum) * 100
                PieChartData(
                    category = category,
                    total = total,
                    percentage = percentage
                )
            }.filter { it.total > 0 }.sortedByDescending { it.total }
        } else {
            emptyList()
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundGradient(isLight))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.Center)
            )
        } else if (error != null) {
            Column(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Ошибка: $error",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { viewModel.refresh() }) {
                    Text("Повторить")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Переключатель Расходы/Доходы
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransactionType.values().forEach { type ->
                            FilterChip(
                                selected = selectedTransactionType == type,
                                onClick = { selectedTransactionType = type },
                                label = { Text(if (type == TransactionType.EXPENSES) "Расходы" else "Доходы") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Фильтры
                item {
                    LiquidGlassCard(isLight = isLight) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Период
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Период",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.secondaryText(isLight)
                                )
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    FilterChip(
                                        selected = false,
                                        onClick = { expanded = true },
                                        label = { Text(selectedPeriod.displayName) }
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        PeriodFilter.values().forEach { period ->
                                            DropdownMenuItem(
                                                text = { Text(period.displayName) },
                                                onClick = {
                                                    selectedPeriod = period
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Выбор дат (если выбран custom)
                            if (selectedPeriod == PeriodFilter.CUSTOM) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "От",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.secondaryText(isLight)
                                        )
                                        TextButton(
                                            onClick = { showStartDatePicker = true }
                                        ) {
                                            Text(
                                                text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(startDate),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = AppColors.primaryText(isLight)
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "До",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.secondaryText(isLight)
                                        )
                                        TextButton(
                                            onClick = { showEndDatePicker = true }
                                        ) {
                                            Text(
                                                text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(endDate),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = AppColors.primaryText(isLight)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Категория
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Категория",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = AppColors.secondaryText(isLight)
                                )
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    FilterChip(
                                        selected = selectedCategory != null,
                                        onClick = { expanded = true },
                                        label = { Text(selectedCategory?.name ?: "Все категории") }
                                    )
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Все категории") },
                                            onClick = {
                                                selectedCategory = null
                                                expanded = false
                                            }
                                        )
                                        categories.forEach { category ->
                                            DropdownMenuItem(
                                                text = { Text(category.name) },
                                                onClick = {
                                                    selectedCategory = category
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Статистика
                item {
                    LiquidGlassCard(isLight = isLight) {
                        Column {
                            Text(
                                text = if (selectedTransactionType == TransactionType.EXPENSES) "Всего расходов" else "Всего доходов",
                                style = MaterialTheme.typography.titleMedium,
                                color = AppColors.secondaryText(isLight)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currencyFormat.format(totalFiltered),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = if (selectedTransactionType == TransactionType.EXPENSES) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    Color(0xFF34C759)
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${filteredExpenses.size} операций",
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppColors.secondaryText(isLight)
                            )
                        }
                    }
                }
                
                // Диаграмма распределения по категориям (для расходов)
                if (selectedTransactionType == TransactionType.EXPENSES && categoryChartData.isNotEmpty()) {
                    item {
                        LiquidGlassCard(isLight = isLight) {
                            Column {
                                Text(
                                    text = "Распределение по категориям",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppColors.primaryText(isLight)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Диаграмма и легенда
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    PieChartView(
                                        data = categoryChartData,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(200.dp),
                                        isLight = isLight
                                    )
                                    
                                    // Легенда
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        categoryChartData.take(5).forEach { item ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(
                                                            item.category.getColor(),
                                                            androidx.compose.foundation.shape.CircleShape
                                                        )
                                                )
                                                Text(
                                                    text = item.category.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AppColors.primaryText(isLight),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${item.percentage.toInt()}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AppColors.primaryText(isLight)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Диаграмма распределения по категориям (для доходов)
                if (selectedTransactionType == TransactionType.INCOME && incomeChartData.isNotEmpty()) {
                    item {
                        LiquidGlassCard(isLight = isLight) {
                            Column {
                                Text(
                                    text = "Распределение по категориям",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppColors.primaryText(isLight)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Диаграмма и легенда
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    PieChartView(
                                        data = incomeChartData,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(200.dp),
                                        isLight = isLight
                                    )
                                    
                                    // Легенда
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        incomeChartData.take(5).forEach { item ->
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .background(
                                                            item.category.getColor(),
                                                            androidx.compose.foundation.shape.CircleShape
                                                        )
                                                )
                                                Text(
                                                    text = item.category.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = AppColors.primaryText(isLight),
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "${item.percentage.toInt()}%",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AppColors.primaryText(isLight)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Список расходов
                items(
                    items = filteredExpenses,
                    key = { it.id.toString() }
                ) { expense ->
                    ExpenseRow(
                        expense = expense,
                        currencyFormat = currencyFormat,
                        isLight = isLight,
                        onClick = { selectedExpense = expense }
                    )
                }
            }
        }
    }
    
    // Date Pickers
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.time
        )
        DatePickerDialog(
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
            },
            onDismiss = { showStartDatePicker = false },
            datePickerState = datePickerState
        )
    }
    
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.time
        )
        DatePickerDialog(
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            onDismiss = { showEndDatePicker = false },
            datePickerState = datePickerState
        )
    }
    
    selectedExpense?.let { expense ->
        ModalBottomSheet(
            onDismissRequest = { selectedExpense = null },
            containerColor = AppColors.cardBackground(isLight)
        ) {
            EditExpenseScreen(
                expense = expense,
                expenseRepository = repository,
                onExpenseUpdated = {
                    viewModel.refresh()
                    selectedExpense = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit,
    datePickerState: DatePickerState
) {
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onDateSelected(Date(it))
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseRow(
    expense: Expense,
    currencyFormat: NumberFormat,
    isLight: Boolean,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        LiquidGlassCard(isLight = isLight) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = expense.category.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = AppColors.primaryText(isLight)
                    )
                    val noteText = expense.note
                    if (noteText != null && noteText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = noteText,
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.secondaryText(isLight)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                            .format(Date(expense.date)),
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.secondaryText(isLight)
                    )
                }
                Text(
                    text = if (expense.type == Expense.ExpenseType.EXPENSE) {
                        "-${currencyFormat.format(expense.amount.toDouble())}"
                    } else {
                        "+${currencyFormat.format(expense.amount.toDouble())}"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = if (expense.type == Expense.ExpenseType.EXPENSE) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color(0xFF34C759)
                    }
                )
            }
        }
    }
}
