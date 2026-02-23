package com.islamic.prayertimes.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.islamic.prayertimes.R
import com.islamic.prayertimes.data.repository.PrayerRepository
import com.islamic.prayertimes.presentation.main.MainActivity
import com.islamic.prayertimes.utils.AlarmScheduler
import com.islamic.prayertimes.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AzanForegroundService : Service() {

    @Inject
    lateinit var prayerRepository: PrayerRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private var mediaPlayer: MediaPlayer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        fun startService(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: return START_NOT_STICKY
        val prayerName = intent.getStringExtra("PRAYER_NAME") ?: ""

        startForeground(Constants.SERVICE_NOTIFICATION_ID, createNotification())

        when (action) {
            "PLAY_AZAN" -> {
                serviceScope.launch {
                    handleAzanTime(prayerName)
                }
            }
            "SHOW_REMINDER" -> {
                val minutesBefore = intent.getIntExtra("MINUTES_BEFORE", 0)
                showReminderNotification(prayerName, minutesBefore)
                stopSelf()
            }
            "STOP_AZAN" -> {
                stopAzan()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private suspend fun handleAzanTime(prayerName: String) {
        try {
            val settings = prayerRepository.getSettingsOnce()

            // Check silent mode
            if (settings.respectSilentMode && isPhoneInSilentMode()) {
                showSilentNotification(prayerName)
                rescheduleNextDayAlarms()
                stopSelf()
                return
            }

            // Play Azan
            playAzan(settings.azanSound.resourceName, prayerName)

            // Show notification
            showAzanNotification(prayerName)

            // Reschedule for next day after playing
            rescheduleNextDayAlarms()

        } catch (e: Exception) {
            Log.e("AzanService", "Error handling azan", e)
            stopSelf()
        }
    }

    private fun playAzan(azanSoundName: String, prayerName: String) {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

            // Set volume to maximum for Azan
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0)

            val resourceId = when (azanSoundName) {
                Constants.AZAN_MAKKAH -> R.raw.azan_makkah
                Constants.AZAN_MADINAH -> R.raw.azan_madinah
                Constants.AZAN_EGYPT -> R.raw.azan_egypt
                else -> R.raw.azan_makkah
            }

            mediaPlayer = MediaPlayer.create(this, resourceId).apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )

                setOnCompletionListener {
                    // Restore original volume
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
                    stopSelf()
                }

                setOnErrorListener { _, _, _ ->
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0)
                    stopSelf()
                    true
                }

                start()
            }

            Log.d("AzanService", "Playing Azan for $prayerName")
        } catch (e: Exception) {
            Log.e("AzanService", "Error playing Azan", e)
            stopSelf()
        }
    }

    private fun stopAzan() {
        mediaPlayer?.apply {
            if (isPlaying) {
                stop()
            }
            release()
        }
        mediaPlayer = null
    }

    private fun isPhoneInSilentMode(): Boolean {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return audioManager.ringerMode != AudioManager.RINGER_MODE_NORMAL
    }

    private fun showAzanNotification(prayerName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val fullScreenIntent = Intent(this, MainActivity::class.java)
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, AzanForegroundService::class.java).apply {
            action = "STOP_AZAN"
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.AZAN_CHANNEL_ID)
            .setContentTitle("It's time for $prayerName prayer")
            .setContentText("Azan is playing...")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .build()

        notificationManager.notify(Constants.AZAN_NOTIFICATION_ID, notification)
    }

    private fun showReminderNotification(prayerName: String, minutesBefore: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.REMINDER_CHANNEL_ID)
            .setContentTitle("Prayer Reminder")
            .setContentText("$prayerName prayer is in $minutesBefore minutes")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Constants.REMINDER_NOTIFICATION_ID, notification)
    }

    private fun showSilentNotification(prayerName: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, Constants.AZAN_CHANNEL_ID)
            .setContentTitle("It's time for $prayerName prayer")
            .setContentText("Azan not played (Silent mode)")
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(Constants.AZAN_NOTIFICATION_ID, notification)
    }

    private suspend fun rescheduleNextDayAlarms() {
        try {
            val settings = prayerRepository.getSettingsOnce()
            val tomorrow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }.time

            val prayerTimes = prayerRepository.getPrayerTimes(tomorrow, settings)
            if (prayerTimes != null) {
                alarmScheduler.scheduleAllPrayers(prayerTimes, settings)
                Log.d("AzanService", "Rescheduled alarms for next day")
            }
        } catch (e: Exception) {
            Log.e("AzanService", "Error rescheduling alarms", e)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.SERVICE_CHANNEL_ID)
            .setContentTitle("Islamic Prayer Times")
            .setContentText("Running in background")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "IslamicPrayerTimes::AzanWakeLock"
        ).apply {
            acquire(5 * 60 * 1000L) // 5 minutes max
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAzan()
        wakeLock?.release()
        serviceScope.cancel()
    }
}