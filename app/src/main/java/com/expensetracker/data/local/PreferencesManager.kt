package com.expensetracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val IS_AUTHENTICATED_KEY = booleanPreferencesKey("is_authenticated")
        private val COLOR_SCHEME_KEY = stringPreferencesKey("color_scheme")
        private val HAS_COMPLETED_ONBOARDING_KEY = booleanPreferencesKey("has_completed_onboarding")
        private val CURRENT_USER_ID_KEY = stringPreferencesKey("current_user_id")
    }
    
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[AUTH_TOKEN_KEY]
    }
    
    val isAuthenticated: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_AUTHENTICATED_KEY] ?: false
    }
    
    val colorScheme: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[COLOR_SCHEME_KEY] ?: "system"
    }
    
    val hasCompletedOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_COMPLETED_ONBOARDING_KEY] ?: false
    }
    
    val currentUserId: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[CURRENT_USER_ID_KEY]
    }
    
    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[IS_AUTHENTICATED_KEY] = true
        }
    }
    
    suspend fun clearAuthToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(AUTH_TOKEN_KEY)
            preferences[IS_AUTHENTICATED_KEY] = false
            preferences.remove(CURRENT_USER_ID_KEY)
        }
    }
    
    suspend fun saveColorScheme(scheme: String) {
        context.dataStore.edit { preferences ->
            preferences[COLOR_SCHEME_KEY] = scheme
        }
    }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_COMPLETED_ONBOARDING_KEY] = completed
        }
    }
    
    suspend fun saveCurrentUserId(userId: String) {
        context.dataStore.edit { preferences ->
            preferences[CURRENT_USER_ID_KEY] = userId
        }
    }
    
    fun getAuthTokenSync(): String? {
        return runBlocking {
            context.dataStore.data.first()[AUTH_TOKEN_KEY]
        }
    }
}

