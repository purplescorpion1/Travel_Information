package com.travelplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.travelplanner.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application)

    val apiUsername: StateFlow<String> = settingsRepository.apiUsernameFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    val apiPassword: StateFlow<String> = settingsRepository.apiPasswordFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun saveCredentials(username: String, token: String) {
        viewModelScope.launch {
            settingsRepository.saveApiCredentials(username, token)
        }
    }

    fun clearCredentials() {
        viewModelScope.launch {
            settingsRepository.clearApiCredentials()
        }
    }
}
