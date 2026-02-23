package com.islamic.prayertimes.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.islamic.prayertimes.data.models.DailyPrayerTimes
import com.islamic.prayertimes.data.models.PrayerSettings
import com.islamic.prayertimes.receivers.PrayerAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAllPrayers(prayerTimes: DailyPrayerTimes, settings: PrayerSettings) {
        val prayers = prayerTimes.getAllPrayers()

        prayers.forEach { prayer ->
            if (prayer.isEnabled) {
                schedulePrayerAlarm(prayer.name, prayer.time)

                // Schedule reminder if enabled
                val reminderEnabled = when (prayer.name) {
                    Constants.FAJR -> settings.fajrReminderEnabled
                    Constants.DHUHR -> settings.dhuhrReminderEnabled
                    Constants.ASR -> settings.asrReminderEnabled
                    Constants.MAGHRIB -> settings.maghribReminderEnabled
                    Constants.ISHA -> settings.ishaReminderEnabled
                    else -> false
                }

                if (reminderEnabled) {
                    scheduleReminderAlarm(
                        prayer.name,
                        prayer.time,
                        settings.reminderMinutes
                    )
                }
            }
        }

        Log.d("AlarmScheduler", "All prayer alarms scheduled")
    }

    private fun schedulePrayerAlarm(prayerName: String, time: Date) {
        val requestCode = getAlarmRequestCode(prayerName)
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "PRAYER_ALARM"
            putExtra("PRAYER_NAME", prayerName)
            putExtra("IS_REMINDER", false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(time.time, pendingIntent)
        Log.d("AlarmScheduler", "Scheduled $prayerName at $time")
    }

    private fun scheduleReminderAlarm(prayerName: String, time: Date, minutesBefore: Int) {
        val requestCode = getReminderRequestCode(prayerName)
        val reminderTime = Calendar.getInstance().apply {
            timeInMillis = time.time
            add(Calendar.MINUTE, -minutesBefore)
        }

        // Don't schedule if reminder time is in the past
        if (reminderTime.timeInMillis <= System.currentTimeMillis()) {
            return
        }

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "PRAYER_REMINDER"
            putExtra("PRAYER_NAME", prayerName)
            putExtra("IS_REMINDER", true)
            putExtra("MINUTES_BEFORE", minutesBefore)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleExactAlarm(reminderTime.timeInMillis, pendingIntent)
        Log.d("AlarmScheduler", "Scheduled reminder for $prayerName at ${reminderTime.time}")
    }

    private fun scheduleExactAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun cancelAllAlarms() {
        val prayers = listOf(
            Constants.FAJR,
            Constants.DHUHR,
            Constants.ASR,
            Constants.MAGHRIB,
            Constants.ISHA
        )

        prayers.forEach { prayer ->
            cancelPrayerAlarm(prayer)
            cancelReminderAlarm(prayer)
        }

        Log.d("AlarmScheduler", "All alarms cancelled")
    }

    private fun cancelPrayerAlarm(prayerName: String) {
        val requestCode = getAlarmRequestCode(prayerName)
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun cancelReminderAlarm(prayerName: String) {
        val requestCode = getReminderRequestCode(prayerName)
        val intent = Intent(context, PrayerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getAlarmRequestCode(prayerName: String): Int {
        return when (prayerName) {
            Constants.FAJR -> Constants.FAJR_ALARM_ID
            Constants.DHUHR -> Constants.DHUHR_ALARM_ID
            Constants.ASR -> Constants.ASR_ALARM_ID
            Constants.MAGHRIB -> Constants.MAGHRIB_ALARM_ID
            Constants.ISHA -> Constants.ISHA_ALARM_ID
            else -> 0
        }
    }

    private fun getReminderRequestCode(prayerName: String): Int {
        return when (prayerName) {
            Constants.FAJR -> Constants.FAJR_REMINDER_ID
            Constants.DHUHR -> Constants.DHUHR_REMINDER_ID
            Constants.ASR -> Constants.ASR_REMINDER_ID
            Constants.MAGHRIB -> Constants.MAGHRIB_REMINDER_ID
            Constants.ISHA -> Constants.ISHA_REMINDER_ID
            else -> 0
        }
    }
}