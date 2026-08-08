package com.travelplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.travelplanner.api.RetrofitClient
import com.travelplanner.model.api.ServiceDetail
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.create(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _serviceDetail = MutableStateFlow<ServiceDetail?>(null)
    val serviceDetail: StateFlow<ServiceDetail?> = _serviceDetail

    fun loadServiceDetails(uniqueIdentity: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _serviceDetail.value = null
            try {
                val response = api.getServiceDetails(uniqueIdentity = uniqueIdentity, detailed = true)
                _serviceDetail.value = response.service
            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.localizedMessage ?: "Failed to load journey calling points."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
