package com.travelplanner.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.travelplanner.db.AppDatabase
import com.travelplanner.model.FavoriteStation
import com.travelplanner.model.Station
import com.travelplanner.repository.StationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val stationRepository = StationRepository(application)
    private val favoriteStationDao = AppDatabase.getDatabase(application).favoriteStationDao()

    val searchQuery = MutableStateFlow("")
    val suggestions = MutableStateFlow<List<Station>>(emptyList())

    val favorites: StateFlow<List<FavoriteStation>> = favoriteStationDao.getAllFavoritesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Search future date and time state
    val selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())
    val selectedTime = MutableStateFlow<LocalTime>(LocalTime.now())

    // To check if custom future date/time search is enabled
    val isCustomSearchActive = MutableStateFlow(false)

    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
        if (newQuery.isEmpty()) {
            suggestions.value = emptyList()
        } else {
            suggestions.value = stationRepository.getSuggestions(newQuery)
        }
    }

    fun getClosestMatch(query: String): Station? {
        return stationRepository.getClosestMatch(query)
    }

    fun toggleFavorite(crs: String, name: String) {
        viewModelScope.launch {
            if (favoriteStationDao.isFavorite(crs)) {
                favoriteStationDao.deleteFavorite(FavoriteStation(crs, name))
            } else {
                favoriteStationDao.insertFavorite(FavoriteStation(crs, name))
            }
        }
    }

    fun isFavoriteFlow(crs: String): StateFlow<Boolean> {
        val flow = MutableStateFlow(false)
        viewModelScope.launch {
            favoriteStationDao.isFavoriteFlow(crs).collect {
                flow.value = it
            }
        }
        return flow
    }

    fun setCustomDate(date: LocalDate) {
        selectedDate.value = date
        isCustomSearchActive.value = true
    }

    fun setCustomTime(time: LocalTime) {
        selectedTime.value = time
        isCustomSearchActive.value = true
    }

    fun resetToCurrentDateTime() {
        selectedDate.value = LocalDate.now()
        selectedTime.value = LocalTime.now()
        isCustomSearchActive.value = false
    }

    fun getFormattedDateTimeString(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val combined = selectedDate.value.atTime(selectedTime.value)
        return combined.format(formatter)
    }
}
