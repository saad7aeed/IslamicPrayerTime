package com.islamic.prayertimes.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.islamic.prayertimes.data.models.*
import com.islamic.prayertimes.data.repository.PrayerRepository
import com.islamic.prayertimes.utils.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prayerRepository: PrayerRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _settings = MutableStateFlow<PrayerSettings?>(null)
    val settings: StateFlow<PrayerSettings?> = _settings.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            prayerRepository.getSettings().collect { settings ->
                _settings.value = settings
            }
        }
    }

    fun updateCalculationMethod(method: PrayerCalculationMethod) {  // Changed here
        updateSettings { it.copy(calculationMethod = method) }
    }

    fun updateAzanSound(sound: AzanSound) {
        updateSettings { it.copy(azanSound = sound) }
    }

    fun updateReminderMinutes(minutes: Int) {
        updateSettings { it.copy(reminderMinutes = minutes) }
    }

    fun updateRespectSilentMode(respect: Boolean) {
        updateSettings { it.copy(respectSilentMode = respect) }
    }

    fun updatePrayerEnabled(prayerName: String, enabled: Boolean) {
        updateSettings { currentSettings ->
            when (prayerName) {
                "Fajr" -> currentSettings.copy(fajrEnabled = enabled)
                "Dhuhr" -> currentSettings.copy(dhuhrEnabled = enabled)
                "Asr" -> currentSettings.copy(asrEnabled = enabled)
                "Maghrib" -> currentSettings.copy(maghribEnabled = enabled)
                "Isha" -> currentSettings.copy(ishaEnabled = enabled)
                else -> currentSettings
            }
        }
    }

    fun updateReminderEnabled(prayerName: String, enabled: Boolean) {
        updateSettings { currentSettings ->
            when (prayerName) {
                "Fajr" -> currentSettings.copy(fajrReminderEnabled = enabled)
                "Dhuhr" -> currentSettings.copy(dhuhrReminderEnabled = enabled)
                "Asr" -> currentSettings.copy(asrReminderEnabled = enabled)
                "Maghrib" -> currentSettings.copy(maghribReminderEnabled = enabled)
                "Isha" -> currentSettings.copy(ishaReminderEnabled = enabled)
                else -> currentSettings
            }
        }
    }

    private fun updateSettings(transform: (PrayerSettings) -> PrayerSettings) {
        viewModelScope.launch {
            val currentSettings = prayerRepository.getSettingsOnce()
            val newSettings = transform(currentSettings)
            prayerRepository.updateSettings(newSettings)

            // Reschedule alarms with new settings
            rescheduleAlarms(newSettings)
        }
    }

    private suspend fun rescheduleAlarms(settings: PrayerSettings) {
        try {
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