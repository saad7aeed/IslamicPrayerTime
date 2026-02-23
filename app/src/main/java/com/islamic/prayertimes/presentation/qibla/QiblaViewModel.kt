package com.islamic.prayertimes.presentation.qibla

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.prayertimes.data.models.Location
import com.islamic.prayertimes.data.repository.PrayerRepository
import com.islamic.prayertimes.domain.calculator.PrayerTimeCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QiblaViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val calculator: PrayerTimeCalculator
) : ViewModel() {

    private val _qiblaDirection = MutableStateFlow<Double?>(null)
    val qiblaDirection: StateFlow<Double?> = _qiblaDirection.asStateFlow()

    private val _userLocation = MutableStateFlow<Location?>(null)
    val userLocation: StateFlow<Location?> = _userLocation.asStateFlow()

    init {
        loadQiblaDirection()
    }

    private fun loadQiblaDirection() {
        viewModelScope.launch {
            prayerRepository.getSettings().collect { settings ->
                settings.location?.let { location ->
                    _userLocation.value = location
                    val direction = calculator.calculateQiblaDirection(location)
                    _qiblaDirection.value = direction
                }
            }
        }
    }
}