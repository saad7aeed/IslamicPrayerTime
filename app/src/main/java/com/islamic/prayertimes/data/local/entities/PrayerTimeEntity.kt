package com.islamic.prayertimes.data.local.entities


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prayer_times")
data class PrayerTimeEntity(
    @PrimaryKey
    val date: String, // Format: yyyy-MM-dd
    val fajrTime: Long,
    val dhuhrTime: Long,
    val asrTime: Long,
    val maghribTime: Long,
    val ishaTime: Long,
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 1,
    val calculationMethod: String,
    val azanSound: String,
    val reminderMinutes: Int,
    val respectSilentMode: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val cityName: String?,
    val countryName: String?,

    val fajrEnabled: Boolean,
    val dhuhrEnabled: Boolean,
    val asrEnabled: Boolean,
    val maghribEnabled: Boolean,
    val ishaEnabled: Boolean,

    val fajrReminderEnabled: Boolean,
    val dhuhrReminderEnabled: Boolean,
    val asrReminderEnabled: Boolean,
    val maghribReminderEnabled: Boolean,
    val ishaReminderEnabled: Boolean
)