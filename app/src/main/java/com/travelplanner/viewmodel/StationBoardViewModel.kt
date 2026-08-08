package com.travelplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.travelplanner.api.RetrofitClient
import com.travelplanner.model.api.LocationLineUpObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StationBoardViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.create(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Grouped departures/arrivals by platform. Map of: Platform -> Next 3 Trains
    private val _groupedArrivals = MutableStateFlow<Map<String, List<LocationLineUpObject>>>(emptyMap())
    val groupedArrivals: StateFlow<Map<String, List<LocationLineUpObject>>> = _groupedArrivals

    private val _groupedDepartures = MutableStateFlow<Map<String, List<LocationLineUpObject>>>(emptyMap())
    val groupedDepartures: StateFlow<Map<String, List<LocationLineUpObject>>> = _groupedDepartures

    fun loadStationBoard(crs: String, formattedDateTime: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Fetch location lineup (including detailed mode if possible, let's keep detailed = true to see delay reasons!)
                val response = api.getLocationServices(code = crs, timeFrom = formattedDateTime, detailed = true)
                val services = response.services ?: emptyList()

                // Filter & Group Arrivals: services that have planned/advertised arrivals or displayAs call/terminal
                val arrivals = services.filter { service ->
                    service.temporalData?.arrival?.scheduleAdvertised != null &&
                            service.temporalData.displayAs != "PASS"
                }
                _groupedArrivals.value = groupServicesByPlatform(arrivals)

                // Filter & Group Departures: services that have planned/advertised departures
                val departures = services.filter { service ->
                    service.temporalData?.departure?.scheduleAdvertised != null &&
                            service.temporalData.displayAs != "PASS"
                }
                _groupedDepartures.value = groupServicesByPlatform(departures)

            } catch (e: Exception) {
                e.printStackTrace()
                _error.value = e.localizedMessage ?: "Failed to load train times. Please check settings / internet."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun groupServicesByPlatform(services: List<LocationLineUpObject>): Map<String, List<LocationLineUpObject>> {
        // Map: Platform -> List of services
        val platformMap = mutableMapOf<String, MutableList<LocationLineUpObject>>()

        for (service in services) {
            val platform = service.locationMetadata?.platform?.actual
                ?: service.locationMetadata?.platform?.planned
                ?: "TBC"

            val list = platformMap.getOrPut(platform) { mutableListOf() }
            list.add(service)
        }

        // For each platform, sort services by advertised time and take the next 3
        return platformMap.mapValues { (_, list) ->
            list.sortedBy { service ->
                val timeStr = service.temporalData?.arrival?.scheduleAdvertised
                    ?: service.temporalData?.departure?.scheduleAdvertised
                    ?: ""
                timeStr
            }.take(3)
        }.toSortedMap()
    }
}
