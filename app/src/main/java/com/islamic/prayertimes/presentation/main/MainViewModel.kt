package com.islamic.prayertimes.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.prayertimes.data.models.DailyPrayerTimes
import com.islamic.prayertimes.data.models.Location
import com.islamic.prayertimes.data.models.PrayerSettings
import com.islamic.prayertimes.data.models.PrayerTime
import com.islamic.prayertimes.data.repository.LocationRepository
import com.islamic.prayertimes.data.repository.PrayerRepository
import com.islamic.prayertimes.utils.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val locationRepository: LocationRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _settings = MutableStateFlow<PrayerSettings?>(null)
    val settings: StateFlow<PrayerSettings?> = _settings.asStateFlow()

    private var countdownJob: Job? = null

    init {
        observeSettings()
        loadPrayerTimes()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            prayerRepository.getSettings().collect { settings ->
                _settings.value = settings
                if (settings.location != null) {
                    loadPrayerTimes()
                }
            }
        }
    }

    fun loadPrayerTimes() {
        viewModelScope.launch {
            try {
                val settings = prayerRepository.getSettingsOnce()

                if (settings.location == null) {
                    _uiState.value = MainUiState.LocationNotSet
                    return@launch
                }

                val prayerTimes = prayerRepository.getPrayerTimes(Date(), settings)

                if (prayerTimes != null) {
                    _uiState.value = MainUiState.Success(prayerTimes)
                    startCountdown(prayerTimes)

                    // Schedule alarms
                    alarmScheduler.scheduleAllPrayers(prayerTimes, settings)
                } else {
                    _uiState.value = MainUiState.Error("Unable to calculate prayer times")
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun fetchLocation() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading

            locationRepository.getCurrentLocation()
                .onSuccess { location ->
                    updateLocation(location)
                }
                .onFailure { error ->
                    _uiState.value = MainUiState.Error(
                        error.message ?: "Failed to get location"
                    )
                }
        }
    }

    fun updateLocation(location: Location) {
        viewModelScope.launch {
            try {
                val currentSettings = prayerRepository.getSettingsOnce()
                val newSettings = currentSettings.copy(location = location)
                prayerRepository.updateSettings(newSettings)
                loadPrayerTimes()
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "Failed to update location")
            }
        }
    }

    private fun startCountdown(prayerTimes: DailyPrayerTimes) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            while (isActive) {
                val currentTime = Date()
                val nextPrayer = prayerTimes.getNextPrayer(currentTime)

                if (nextPrayer != null) {
                    val timeRemaining = calculateTimeRemaining(currentTime, nextPrayer.time)
                    _uiState.value = MainUiState.Success(
                        prayerTimes = prayerTimes,
                        nextPrayer = nextPrayer,
                        timeRemaining = timeRemaining
                    )
                }

                delay(1000) // Update every second
            }
        }
    }

    private fun calculateTimeRemaining(currentTime: Date, prayerTime: Date): String {
        val diff = prayerTime.time - currentTime.time

        if (diff <= 0) return "Now"

        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff) % 60

        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }

    fun rescheduleAlarms() {
        viewModelScope.launch {
            try {
                val settings = prayerRepository.getSettingsOnce()
                val prayerTimes = prayerRepository.getPrayerTimes(Date(), settings)

                if (prayerTimes != null) {
                    alarmScheduler.cancelAllAlarms()
                    alarmScheduler.scheduleAllPrayers(prayerTimes, settings)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

sealed class MainUiState {
    object Loading : MainUiState()
    object LocationNotSet : MainUiState()
    data class Success(
        val prayerTimes: DailyPrayerTimes,
        val nextPrayer: PrayerTime? = null,
        val timeRemaining: String = ""
    ) : MainUiState()
    data class Error(val message: String) : MainUiState()
}