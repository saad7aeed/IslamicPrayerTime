package com.islamic.prayertimes.data.models


data class PrayerSettings(
    val calculationMethod: PrayerCalculationMethod = PrayerCalculationMethod.MWL,  // Changed
    val azanSound: AzanSound = AzanSound.MAKKAH,
    val reminderMinutes: Int = 10,
    val respectSilentMode: Boolean = true,
    val location: Location? = null,

    val fajrEnabled: Boolean = true,
    val dhuhrEnabled: Boolean = true,
    val asrEnabled: Boolean = true,
    val maghribEnabled: Boolean = true,
    val ishaEnabled: Boolean = true,

    val fajrReminderEnabled: Boolean = false,
    val dhuhrReminderEnabled: Boolean = false,
    val asrReminderEnabled: Boolean = false,
    val maghribReminderEnabled: Boolean = false,
    val ishaReminderEnabled: Boolean = false
)