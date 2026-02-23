package com.islamic.prayertimes.data.local

import androidx.room.*
import com.islamic.prayertimes.data.local.entities.PrayerTimeEntity
import com.islamic.prayertimes.data.local.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {
    @Query("SELECT * FROM prayer_times WHERE date = :date")
    suspend fun getPrayerTimesByDate(date: String): PrayerTimeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTimes(prayerTime: PrayerTimeEntity)

    @Query("DELETE FROM prayer_times WHERE date < :date")
    suspend fun deleteOldPrayerTimes(date: String)

    @Query("SELECT * FROM settings WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    @Query("SELECT * FROM settings WHERE id = 1")
    suspend fun getSettingsOnce(): SettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SettingsEntity)

    @Update
    suspend fun updateSettings(settings: SettingsEntity)
}