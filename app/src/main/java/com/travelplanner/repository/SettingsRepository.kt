package com.travelplanner.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "travel_planner_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val API_USERNAME_KEY = stringPreferencesKey("rtt_api_username")
        private val API_PASSWORD_KEY = stringPreferencesKey("rtt_api_password")
    }

    val apiUsernameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_USERNAME_KEY] ?: ""
    }

    val apiPasswordFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[API_PASSWORD_KEY] ?: ""
    }

    suspend fun saveApiCredentials(username: String, token: String) {
        context.dataStore.edit { preferences ->
            preferences[API_USERNAME_KEY] = username
            preferences[API_PASSWORD_KEY] = token
        }
    }

    suspend fun clearApiCredentials() {
        context.dataStore.edit { preferences ->
            preferences.remove(API_USERNAME_KEY)
            preferences.remove(API_PASSWORD_KEY)
        }
    }
}
