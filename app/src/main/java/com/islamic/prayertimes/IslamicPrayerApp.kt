package com.islamic.prayertimes

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.islamic.prayertimes.utils.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class IslamicPrayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Azan Notification Channel
            val azanChannel = NotificationChannel(
                Constants.AZAN_CHANNEL_ID,
                "Prayer Times (Azan)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for prayer times with Azan"
                enableVibration(true)
                setSound(null, null) // We'll handle sound manually
            }

            // Reminder Notification Channel
            val reminderChannel = NotificationChannel(
                Constants.REMINDER_CHANNEL_ID,
                "Prayer Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders before prayer times"
                enableVibration(true)
            }

            // Foreground Service Channel
            val serviceChannel = NotificationChannel(
                Constants.SERVICE_CHANNEL_ID,
                "Prayer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background service for prayer times"
            }

            notificationManager.createNotificationChannel(azanChannel)
            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }
}