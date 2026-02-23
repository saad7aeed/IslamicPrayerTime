package com.islamic.prayertimes.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.islamic.prayertimes.services.AzanForegroundService
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("PrayerAlarmReceiver", "Alarm received: ${intent.action}")

        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: return
        val isReminder = intent.getBooleanExtra("IS_REMINDER", false)
        val minutesBefore = intent.getIntExtra("MINUTES_BEFORE", 0)

        // Start foreground service to play Azan or show notification
        val serviceIntent = Intent(context, AzanForegroundService::class.java).apply {
            action = if (isReminder) "SHOW_REMINDER" else "PLAY_AZAN"
            putExtra("PRAYER_NAME", prayerName)
            putExtra("MINUTES_BEFORE", minutesBefore)
        }

        AzanForegroundService.startService(context, serviceIntent)
    }
}