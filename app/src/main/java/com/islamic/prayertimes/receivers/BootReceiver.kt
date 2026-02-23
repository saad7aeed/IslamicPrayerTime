package com.islamic.prayertimes.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.islamic.prayertimes.data.repository.PrayerRepository
import com.islamic.prayertimes.utils.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var prayerRepository: PrayerRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, rescheduling alarms")

            scope.launch {
                try {
                    val settings = prayerRepository.getSettingsOnce()
                    val prayerTimes = prayerRepository.getPrayerTimes(Date(), settings)

                    if (prayerTimes != null) {
                        alarmScheduler.scheduleAllPrayers(prayerTimes, settings)
                    }
                } catch (e: Exception) {
                    Log.e("BootReceiver", "Error rescheduling alarms", e)
                }
            }
        }
    }
}