package com.expensetracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.expensetracker.data.models.Expense
import com.expensetracker.data.repository.ExpenseRepository
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.repository.ProfileRepository
import com.expensetracker.data.local.PreferencesManager
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.ui.components.LiquidGlassCard
import com.expensetracker.ui.components.PieChartView
import com.expensetracker.ui.viewmodel.DashboardViewModel
import com.expensetracker.ui.viewmodel.DashboardViewModelFactory
import com.expensetracker.ui.categories.CategoriesScreen
import com.expensetracker.ui.categories.CategoryExpensesScreen
import com.expensetracker.ui.expenses.ExpensesHistoryScreen
import com.expensetracker.ui.utils.AppColors
import java.text.NumberFormat
import java.util.*
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    expenseRepository: ExpenseRepository? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val retrofitClient = remember { RetrofitClient(preferencesManager) }
    val repository = expenseRepository ?: remember { 
        ExpenseRepository(retrofitClient.apiService, retrofitClient) 
    }
    val categoryRepository = remember {
        CategoryRepository(retrofitClient.apiService, retrofitClient)
    }
    val profileRepository = remember {
        ProfileRepository(retrofitClient.apiService, retrofitClient)
    }
    val viewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModelFactory(repository, categoryRepository)
    )
    val expenses by viewModel.expenses.collectAsState()
    val currentMonthExpenses by viewModel.currentMonthExpenses.collectAsState()
    val currentMonthIncome by viewModel.currentMonthIncome.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val categoryChartData by viewModel.categoryChartData.collectAsState()
    var showCategories by remember { mutableStateOf(false) }
    var showExpensesHistory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<com.expensetracker.data.models.Category?>(null) }
    
    var profile by remember { mutableStateOf<com.expensetracker.data.models.UserProfile?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    val isLight = MaterialTheme.colorScheme.surface == Color.White
    val error by viewModel.error.collectAsState()
    
    // Обновляем данные при появлении экрана
    LaunchedEffect(Unit) {
        if (expenses.isEmpty() && !isLoading) {
            viewModel.refresh()
        }
        // Загружаем профиль
        coroutineScope.launch {
            profileRepository.getProfile()
                .onSuccess { userProfile ->
                    profile = userProfile
                }
        }
    }
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply {
            currency = Currency.getInstance("RUB")
            maximumFractionDigits = 0
        }
    }
    
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "Доброе утро"
            in 12..17 -> "Добрый день"
            in 18..22 -> "Добрый вечер"
            else -> "Доброй ночи"
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundGradient(isLight))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (error != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Приветствие и профиль
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.primaryText(isLight)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = profile?.name ?: "Пользователь",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = AppColors.primaryText(isLight)
                            )
                        }
                        
                        // Фото профиля
                        if (profile?.avatar != null) {
                            AsyncImage(
                                model = profile!!.avatar,
                                contentDescription = "Аватар",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = profile?.name?.firstOrNull()?.toString() ?: "?",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // Отступ перед расходами
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Карточка расходов
                item {
                    LiquidGlassCard(
                        isLight = isLight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showExpensesHistory = true }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Всего расходов",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppColors.secondaryText(isLight)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "за месяц",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.secondaryText(isLight)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = AppColors.secondaryText(isLight)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currencyFormat.format(totalExpenses.toDouble()),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = AppColors.primaryText(isLight)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${currentMonthExpenses.size} операций",
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppColors.secondaryText(isLight)
                            )
                        }
                    }
                }
                
                // Карточка доходов
                item {
                    LiquidGlassCard(
                        isLight = isLight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showExpensesHistory = true }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Всего доходов",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = AppColors.secondaryText(isLight)
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "за месяц",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AppColors.secondaryText(isLight)
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = AppColors.secondaryText(isLight)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = currencyFormat.format(totalIncome.toDouble()),
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 36.sp,
                                color = Color(0xFF34C759) // Green
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${currentMonthIncome.size} операций",
                                style = MaterialTheme.typography.bodyLarge,
                                color = AppColors.secondaryText(isLight)
                            )
                        }
                    }
                }
                
                // Круговая диаграмма расходов по категориям
                if (currentMonthExpenses.isNotEmpty() && categoryChartData.isNotEmpty()) {
                    item {
                        LiquidGlassCard(
                            isLight = isLight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCategories = true }
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Распределение расходов",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = AppColors.primaryText(isLight)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "за месяц",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AppColors.secondaryText(isLight)
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp),
                                            tint = AppColors.secondaryText(isLight)
                                        )
                                    }
                                }
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
                
            }
        }
    }
    
    if (showCategories) {
        ModalBottomSheet(
            onDismissRequest = { showCategories = false },
            containerColor = AppColors.cardBackground(isLight)
        ) {
            CategoriesScreen(
                expenseRepository = repository,
                onCategoryClick = { category ->
                    selectedCategory = category
                    showCategories = false
                }
            )
        }
    }
    
    if (showExpensesHistory) {
        ModalBottomSheet(
            onDismissRequest = { showExpensesHistory = false },
            containerColor = AppColors.cardBackground(isLight)
        ) {
            ExpensesHistoryScreen(
                expenseRepository = repository,
                categoryRepository = categoryRepository
            )
        }
    }
    
    selectedCategory?.let { category ->
        ModalBottomSheet(
            onDismissRequest = { selectedCategory = null },
            containerColor = AppColors.cardBackground(isLight)
        ) {
            CategoryExpensesScreen(
                category = category,
                expenseRepository = repository
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseCard(expense: Expense, currencyFormat: NumberFormat, isLight: Boolean) {
    Card(
        onClick = { /* TODO: Navigate to edit expense */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        LiquidGlassCard(isLight = isLight) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
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
                }
                Text(
                    text = if (expense.type == Expense.ExpenseType.EXPENSE) {
                        "-${currencyFormat.format(expense.amount.toDouble())}"
                    } else {
                        "+${currencyFormat.format(expense.amount.toDouble())}"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (expense.type == Expense.ExpenseType.EXPENSE) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color(0xFF34C759) // Green
                    }
                )
            }
        }
    }
}
