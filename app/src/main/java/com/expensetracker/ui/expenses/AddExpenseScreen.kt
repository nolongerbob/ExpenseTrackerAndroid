package com.expensetracker.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import com.expensetracker.data.repository.ExpenseRepository
import com.expensetracker.data.repository.CategoryRepository
import com.expensetracker.data.local.PreferencesManager
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.ui.components.LiquidGlassCard
import com.expensetracker.ui.utils.AppColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    modifier: Modifier = Modifier,
    expenseRepository: ExpenseRepository? = null,
    categoryRepository: CategoryRepository? = null,
    onExpenseAdded: () -> Unit = {}
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
    val viewModel: com.expensetracker.ui.viewmodel.ExpenseViewModel = viewModel(
        factory = com.expensetracker.ui.viewmodel.ExpenseViewModelFactory(expRepo, catRepo)
    )
    
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var transactionType by remember { mutableStateOf("EXPENSE") }
    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColorHex by remember { mutableStateOf("#007AFF") }
    
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLight = MaterialTheme.colorScheme.surface == Color.White
    
    // Обновляем категории при изменении типа транзакции
    LaunchedEffect(transactionType) {
        viewModel.loadCategories()
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.backgroundGradient(isLight))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Переключатель Расход/Доход
            LiquidGlassCard(isLight = isLight) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = transactionType == "EXPENSE",
                        onClick = { transactionType = "EXPENSE" },
                        label = { Text("Расход") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = transactionType == "INCOME",
                        onClick = { transactionType = "INCOME" },
                        label = { Text("Доход") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Поле суммы
            LiquidGlassCard(isLight = isLight) {
                Column {
                    Text(
                        text = "Сумма",
                        style = MaterialTheme.typography.titleSmall,
                        color = AppColors.secondaryText(isLight)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.textFieldText(isLight),
                            unfocusedTextColor = AppColors.textFieldText(isLight)
                        )
                    )
                }
            }
            
            // Выбор категории
            LiquidGlassCard(isLight = isLight) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Категория",
                            style = MaterialTheme.typography.titleSmall,
                            color = AppColors.secondaryText(isLight)
                        )
                        TextButton(
                            onClick = { showCreateCategoryDialog = true }
                        ) {
                            Text("+ Создать")
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val filteredCategories = categories.filter {
                        it.type.name == transactionType
                    }
                    if (filteredCategories.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            filteredCategories.forEach { category ->
                                val selected = selectedCategoryId == category.id
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            if (selected) category.getColor().copy(alpha = 0.15f)
                                            else Color.Transparent
                                        )
                                        .clickable { selectedCategoryId = category.id }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .background(category.getColor(), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = AppColors.primaryText(isLight),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (selected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = "Выбрано",
                                            tint = AppColors.primaryText(isLight)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Нет категорий. Создайте новую.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = AppColors.secondaryText(isLight)
                        )
                    }
                }
            }
            
            // Поле заметки
            LiquidGlassCard(isLight = isLight) {
                Column {
                    Text(
                        text = "Заметка",
                        style = MaterialTheme.typography.titleSmall,
                        color = AppColors.secondaryText(isLight)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Описание") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = AppColors.textFieldText(isLight),
                            unfocusedTextColor = AppColors.textFieldText(isLight)
                        )
                    )
                }
            }
            
            // Кнопка сохранения
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (amountValue > 0 && selectedCategoryId != null) {
                        viewModel.createExpense(
                            amount = amountValue,
                            categoryId = selectedCategoryId,
                            note = note.ifBlank { null },
                            type = transactionType
                        )
                        onExpenseAdded()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && amount.toDoubleOrNull() != null && amount.toDoubleOrNull()!! > 0 && selectedCategoryId != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Сохранить")
                }
            }
        }
    }
    
    // Диалог создания категории
    if (showCreateCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCreateCategoryDialog = false },
            title = { Text("Новая категория") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Название") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val iosPalette = listOf(
                        "#FF3B30", // Red
                        "#FF9500", // Orange
                        "#FFCC00", // Yellow
                        "#34C759", // Green
                        "#5AC8FA", // Light blue
                        "#007AFF", // Blue
                        "#5856D6", // Indigo
                        "#AF52DE", // Purple
                        "#FF2D55"  // Pink
                    )

                    Text(
                        text = "Цвет",
                        style = MaterialTheme.typography.titleSmall,
                        color = AppColors.secondaryText(isLight)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        iosPalette.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (newCategoryColorHex == hex) 3.dp else 1.dp,
                                        color = if (newCategoryColorHex == hex) {
                                            MaterialTheme.colorScheme.onSurface
                                        } else {
                                            Color.White.copy(alpha = 0.6f)
                                        },
                                        shape = CircleShape
                                    )
                                    .clickable { newCategoryColorHex = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.createCategory(
                                name = newCategoryName,
                                color = newCategoryColorHex,
                                type = transactionType
                            )
                            newCategoryName = ""
                            newCategoryColorHex = "#007AFF"
                            showCreateCategoryDialog = false
                        }
                    },
                    enabled = newCategoryName.isNotBlank()
                ) {
                    Text("Создать")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCreateCategoryDialog = false
                    newCategoryName = ""
                }) {
                    Text("Отмена")
                }
            }
        )
    }
}
