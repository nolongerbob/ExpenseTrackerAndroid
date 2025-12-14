package com.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import com.expensetracker.data.local.PreferencesManager
import com.expensetracker.data.remote.RetrofitClient
import com.expensetracker.data.repository.ExpenseRepository
import com.expensetracker.data.remote.ApiService
import com.expensetracker.ui.auth.LoginScreen
import com.expensetracker.ui.auth.RegisterScreen
import com.expensetracker.ui.MainTabView
import com.expensetracker.ui.navigation.Screen
import com.expensetracker.ui.onboarding.OnboardingScreen
import com.expensetracker.ui.theme.ExpenseTrackerTheme
import com.expensetracker.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize dependencies
        val preferencesManager = PreferencesManager(this)
        val retrofitClient = RetrofitClient(preferencesManager)
        val apiService = retrofitClient.apiService
        val expenseRepository = ExpenseRepository(apiService, retrofitClient)
        
        setContent {
            ExpenseTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExpenseTrackerApp(
                        preferencesManager = preferencesManager,
                        apiService = apiService,
                        expenseRepository = expenseRepository
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseTrackerApp(
    preferencesManager: PreferencesManager,
    apiService: ApiService,
    expenseRepository: ExpenseRepository
) {
    var currentScreen by remember { mutableStateOf<Screen?>(null) }
    var hasCompletedOnboarding by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Check onboarding status
    LaunchedEffect(Unit) {
        preferencesManager.hasCompletedOnboarding.collect { completed ->
            hasCompletedOnboarding = completed
            if (!completed) {
                currentScreen = Screen.Onboarding
            }
        }
    }
    
    // Check authentication status
    LaunchedEffect(Unit) {
        preferencesManager.isAuthenticated.collect { authenticated ->
            isAuthenticated = authenticated
            if (authenticated && hasCompletedOnboarding) {
                currentScreen = Screen.Dashboard
            } else if (!authenticated && hasCompletedOnboarding) {
                currentScreen = Screen.Login
            }
        }
    }
    
    when (currentScreen) {
        Screen.Onboarding -> {
            OnboardingScreen(
                onComplete = {
                    coroutineScope.launch {
                        preferencesManager.setOnboardingCompleted(true)
                        currentScreen = Screen.Login
                    }
                },
                onSkip = {
                    coroutineScope.launch {
                        preferencesManager.setOnboardingCompleted(true)
                        currentScreen = Screen.Login
                    }
                }
            )
        }
        Screen.Login -> {
            val authViewModel = remember {
                AuthViewModel(apiService, preferencesManager)
            }
            LoginScreen(
                onLoginSuccess = {
                    currentScreen = Screen.Dashboard
                },
                onNavigateToRegister = {
                    currentScreen = Screen.Register
                },
                viewModel = authViewModel
            )
        }
        Screen.Register -> {
            val authViewModel = remember {
                AuthViewModel(apiService, preferencesManager)
            }
            RegisterScreen(
                onRegisterSuccess = {
                    currentScreen = Screen.Dashboard
                },
                onNavigateToLogin = {
                    currentScreen = Screen.Login
                },
                viewModel = authViewModel
            )
        }
        Screen.Dashboard -> {
            MainTabView()
        }
        Screen.Expenses,
        Screen.History,
        Screen.Notes,
        Screen.Profile,
        Screen.AddExpense,
        Screen.EditExpense,
        Screen.AddNote,
        Screen.EditNote,
        Screen.Settings -> {
            // Все эти экраны доступны через MainTabView
            MainTabView()
        }
        null -> {
            // Loading state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
