package com.islamic.prayertimes.data.repository

import com.islamic.prayertimes.data.local.PrayerDao
import com.islamic.prayertimes.data.local.entities.PrayerTimeEntity
import com.islamic.prayertimes.data.local.entities.SettingsEntity
import com.islamic.prayertimes.data.models.*
import com.islamic.prayertimes.domain.calculator.PrayerTimeCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrayerRepository @Inject constructor(
    private val prayerDao: PrayerDao,
    private val calculator: PrayerTimeCalculator
) {

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun getSettings(): Flow<PrayerSettings> {
        return prayerDao.getSettings().map { entity ->
            entity?.toPrayerSettings() ?: getDefaultSettings()
        }
    }

    suspend fun getSettingsOnce(): PrayerSettings {
        return prayerDao.getSettingsOnce()?.toPrayerSettings() ?: getDefaultSettings()
    }

    suspend fun updateSettings(settings: PrayerSettings) {
        prayerDao.insertSettings(settings.toEntity())
    }

    suspend fun getPrayerTimes(date: Date, settings: PrayerSettings): DailyPrayerTimes? {
        val location = settings.location ?: return null

        // Try to get from database first
        val dateString = dateFormatter.format(date)
        val cached = prayerDao.getPrayerTimesByDate(dateString)

        return if (cached != null) {
            cached.toDailyPrayerTimes(settings)
        } else {
            // Calculate and cache
            val prayerTimes = calculator.calculatePrayerTimes(
                location = location,
                date = date,
                calculationMethod = settings.calculationMethod,
                settings = settings
            )

            prayerDao.insertPrayerTimes(prayerTimes.toEntity(location))
            prayerTimes
        }
    }

    suspend fun cleanOldPrayerTimes() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        prayerDao.deleteOldPrayerTimes(dateFormatter.format(calendar.time))
    }

    private fun getDefaultSettings() = PrayerSettings()

    private fun SettingsEntity.toPrayerSettings() = PrayerSettings(
        calculationMethod = PrayerCalculationMethod.valueOf(calculationMethod),  // CHANGED
        azanSound = AzanSound.valueOf(azanSound),
        reminderMinutes = reminderMinutes,
        respectSilentMode = respectSilentMode,
        location = if (latitude != null && longitude != null) {
            Location(latitude, longitude, cityName ?: "", countryName ?: "")
        } else null,
        fajrEnabled = fajrEnabled,
        dhuhrEnabled = dhuhrEnabled,
        asrEnabled = asrEnabled,
        maghribEnabled = maghribEnabled,
        ishaEnabled = ishaEnabled,
        fajrReminderEnabled = fajrReminderEnabled,
        dhuhrReminderEnabled = dhuhrReminderEnabled,
        asrReminderEnabled = asrReminderEnabled,
        maghribReminderEnabled = maghribReminderEnabled,
        ishaReminderEnabled = ishaReminderEnabled
    )

    private fun PrayerSettings.toEntity() = SettingsEntity(
        calculationMethod = calculationMethod.name,
        azanSound = azanSound.name,
        reminderMinutes = reminderMinutes,
        respectSilentMode = respectSilentMode,
        latitude = location?.latitude,
        longitude = location?.longitude,
        cityName = location?.cityName,
        countryName = location?.countryName,
        fajrEnabled = fajrEnabled,
        dhuhrEnabled = dhuhrEnabled,
        asrEnabled = asrEnabled,
        maghribEnabled = maghribEnabled,
        ishaEnabled = ishaEnabled,
        fajrReminderEnabled = fajrReminderEnabled,
        dhuhrReminderEnabled = dhuhrReminderEnabled,
        asrReminderEnabled = asrReminderEnabled,
        maghribReminderEnabled = maghribReminderEnabled,
        ishaReminderEnabled = ishaReminderEnabled
    )

    private fun DailyPrayerTimes.toEntity(location: Location) = PrayerTimeEntity(
        date = dateFormatter.format(date),
        fajrTime = fajr.time.time,
        dhuhrTime = dhuhr.time.time,
        asrTime = asr.time.time,
        maghribTime = maghrib.time.time,
        ishaTime = isha.time.time,
        latitude = location.latitude,
        longitude = location.longitude
    )

    private fun PrayerTimeEntity.toDailyPrayerTimes(settings: PrayerSettings) = DailyPrayerTimes(
        date = dateFormatter.parse(date) ?: Date(),
        fajr = PrayerTime(
            name = com.islamic.prayertimes.utils.Constants.FAJR,
            time = Date(fajrTime),
            timeString = timeFormatter.format(Date(fajrTime)),
            isEnabled = settings.fajrEnabled
        ),
        dhuhr = PrayerTime(
            name = com.islamic.prayertimes.utils.Constants.DHUHR,
            time = Date(dhuhrTime),
            timeString = timeFormatter.format(Date(dhuhrTime)),
            isEnabled = settings.dhuhrEnabled
        ),
        asr = PrayerTime(
            name = com.islamic.prayertimes.utils.Constants.ASR,
            time = Date(asrTime),
            timeString = timeFormatter.format(Date(asrTime)),
            isEnabled = settings.asrEnabled
        ),
        maghrib = PrayerTime(
            name = com.islamic.prayertimes.utils.Constants.MAGHRIB,
            time = Date(maghribTime),
            timeString = timeFormatter.format(Date(maghribTime)),
            isEnabled = settings.maghribEnabled
        ),
        isha = PrayerTime(
            name = com.islamic.prayertimes.utils.Constants.ISHA,
            time = Date(ishaTime),
            timeString = timeFormatter.format(Date(ishaTime)),
            isEnabled = settings.ishaEnabled
        )
    )
}