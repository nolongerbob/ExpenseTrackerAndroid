package com.expensetracker.ui.expenses

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
                    if (categories.isNotEmpty()) {
                        categories.filter { 
                            it.type.name == transactionType 
                        }.forEach { category ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                RadioButton(
                                    selected = selectedCategoryId == category.id,
                                    onClick = { selectedCategoryId = category.id }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = AppColors.primaryText(isLight)
                                )
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
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("Название") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            viewModel.createCategory(
                                name = newCategoryName,
                                color = "#007AFF", // Синий цвет по умолчанию
                                type = transactionType
                            )
                            newCategoryName = ""
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
