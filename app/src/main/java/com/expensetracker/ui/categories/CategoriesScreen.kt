package com.expensetracker.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.expensetracker.data.models.Category
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.repository.ExpenseRepository
import com.expensetracker.data.local.PreferencesManager
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.ui.components.LiquidGlassCard
import com.expensetracker.ui.utils.AppColors
import java.text.NumberFormat
import java.util.*

@Composable
fun CategoriesScreen(
    modifier: Modifier = Modifier,
    expenseRepository: ExpenseRepository? = null,
    categoryRepository: CategoryRepository? = null,
    onCategoryClick: (Category) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val retrofitClient = remember { RetrofitClient(preferencesManager) }
    val expRepo = expenseRepository ?: remember { 
        ExpenseRepository(retrofitClient.apiService, retrofitClient) 
    }
    val catRepo = categoryRepository ?: remember {
        CategoryRepository(retrofitClient.apiService, retrofitClient)
    }
    val viewModel: com.expensetracker.ui.viewmodel.CategoryViewModel = viewModel(
        factory = com.expensetracker.ui.viewmodel.CategoryViewModelFactory(expRepo, catRepo)
    )
    
    val categoryTotals by viewModel.categoryTotals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    val isLight = MaterialTheme.colorScheme.surface == Color.White
    
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply {
            currency = Currency.getInstance("RUB")
            maximumFractionDigits = 0
        }
    }
    
    // Загружаем данные при открытии экрана
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundGradient(isLight))
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
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
        } else if (categoryTotals.isEmpty()) {
            Column(
                modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Нет категорий",
                    style = MaterialTheme.typography.titleLarge,
                    color = AppColors.secondaryText(isLight)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = categoryTotals,
                    key = { it.category.id }
                ) { item ->
                    CategoryCard(
                        category = item.category,
                        total = item.total,
                        count = item.count,
                        percentage = item.percentage,
                        currencyFormat = currencyFormat,
                        isLight = isLight,
                        onClick = { onCategoryClick(item.category) }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: Category,
    total: Double,
    count: Int,
    percentage: Double,
    currencyFormat: NumberFormat,
    isLight: Boolean = MaterialTheme.colorScheme.surface == Color.White,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        isLight = isLight,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(category.getColor(), androidx.compose.foundation.shape.CircleShape)
                    )
                    Column {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = AppColors.primaryText(isLight)
                        )
                        Text(
                            text = "$count операций",
                            style = MaterialTheme.typography.bodySmall,
                            color = AppColors.secondaryText(isLight)
                        )
                    }
                }
                Text(
                    text = currencyFormat.format(total),
                    style = MaterialTheme.typography.titleMedium,
                    color = AppColors.primaryText(isLight)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Прогресс-бар
            LinearProgressIndicator(
                progress = (percentage / 100).toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = category.getColor(),
                trackColor = if (isLight) Color(0xFFE5E5EA) else Color.White.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "${String.format(Locale.getDefault(), "%.1f", percentage)}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.secondaryText(isLight)
                )
            }
        }
    }
}

