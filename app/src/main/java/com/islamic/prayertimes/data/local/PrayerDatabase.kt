package com.islamic.prayertimes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.islamic.prayertimes.data.local.entities.PrayerTimeEntity
import com.islamic.prayertimes.data.local.entities.SettingsEntity

@Database(
    entities = [PrayerTimeEntity::class, SettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PrayerDatabase : RoomDatabase() {
    abstract fun prayerDao(): PrayerDao
}