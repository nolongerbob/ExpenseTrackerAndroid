package com.expensetracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.expensetracker.ui.dashboard.DashboardScreen
import com.expensetracker.ui.expenses.ExpensesHistoryScreen
import com.expensetracker.ui.expenses.AddExpenseScreen
import com.expensetracker.ui.notes.NotesScreen
import com.expensetracker.ui.profile.ProfileScreen

@Composable
fun MainTabView() {
    var selectedTab by remember { 
        mutableStateOf(0) 
    }
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.PieChart, contentDescription = "Обзор") },
                    label = { Text("Обзор") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "История") },
                    label = { Text("История") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Добавить") },
                    label = { Text("Добавить") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Note, contentDescription = "Заметки") },
                    label = { Text("Заметки") },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Профиль") },
                    label = { Text("Профиль") },
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            0 -> DashboardScreen(Modifier.padding(paddingValues))
            1 -> ExpensesHistoryScreen(Modifier.padding(paddingValues))
            2 -> AddExpenseScreen(Modifier.padding(paddingValues))
            3 -> NotesScreen(Modifier.padding(paddingValues))
            4 -> ProfileScreen(Modifier.padding(paddingValues))
        }
    }
}

