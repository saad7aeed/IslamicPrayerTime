package com.islamic.prayertimes.data.models
import java.util.Date

data class PrayerTime(
    val name: String,
    val time: Date,
    val timeString: String,
    val isEnabled: Boolean = true
)

data class DailyPrayerTimes(
    val date: Date,
    val fajr: PrayerTime,
    val dhuhr: PrayerTime,
    val asr: PrayerTime,
    val maghrib: PrayerTime,
    val isha: PrayerTime
) {
    fun getAllPrayers(): List<PrayerTime> = listOf(fajr, dhuhr, asr, maghrib, isha)

    fun getNextPrayer(currentTime: Date): PrayerTime? {
        return getAllPrayers()
            .filter { it.isEnabled }
            .firstOrNull { it.time.after(currentTime) }
    }
}

data class Location(
    val latitude: Double,
    val longitude: Double,
    val cityName: String = "",
    val countryName: String = ""
)


enum class PrayerCalculationMethod(val displayName: String) {
    MWL("Muslim World League"),
    ISNA("Islamic Society of North America"),
    EGYPT("Egyptian General Authority"),
    MAKKAH("Umm Al-Qura, Makkah"),
    KARACHI("University of Islamic Sciences, Karachi"),
    TEHRAN("Institute of Geophysics, Tehran"),
    JAFARI("Shia Ithna-Ashari"),
    DUBAI("Gulf Region"),
    KUWAIT("Kuwait"),
    QATAR("Qatar"),
    SINGAPORE("Singapore"),
    TURKEY("Turkey"),
    FRANCE("Union Organization Islamic de France"),
    RUSSIA("Spiritual Administration of Muslims of Russia")
}

enum class AzanSound(val resourceName: String) {
    MAKKAH("azan_makkah"),
    MADINAH("azan_madinah"),
    EGYPT("azan_egypt")
}