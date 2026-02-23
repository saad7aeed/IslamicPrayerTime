package com.islamic.prayertimes.utils


object Constants {
    // Notification Channels
    const val AZAN_CHANNEL_ID = "azan_channel"
    const val REMINDER_CHANNEL_ID = "reminder_channel"
    const val SERVICE_CHANNEL_ID = "service_channel"

    // Notification IDs
    const val AZAN_NOTIFICATION_ID = 1001
    const val REMINDER_NOTIFICATION_ID = 1002
    const val SERVICE_NOTIFICATION_ID = 1003

    // Prayer Names
    const val FAJR = "Fajr"
    const val DHUHR = "Dhuhr"
    const val ASR = "Asr"
    const val MAGHRIB = "Maghrib"
    const val ISHA = "Isha"

    // Alarm Request Codes
    const val FAJR_ALARM_ID = 100
    const val DHUHR_ALARM_ID = 200
    const val ASR_ALARM_ID = 300
    const val MAGHRIB_ALARM_ID = 400
    const val ISHA_ALARM_ID = 500

    // Reminder Request Codes (before prayer)
    const val FAJR_REMINDER_ID = 150
    const val DHUHR_REMINDER_ID = 250
    const val ASR_REMINDER_ID = 350
    const val MAGHRIB_REMINDER_ID = 450
    const val ISHA_REMINDER_ID = 550

    // Azan Sound Resources
    const val AZAN_MAKKAH = "azan_makkah"
    const val AZAN_MADINAH = "azan_madinah"
    const val AZAN_EGYPT = "azan_egypt"

    // Datastore Keys
    const val DATASTORE_NAME = "prayer_settings"
    const val KEY_CALCULATION_METHOD = "calculation_method"
    const val KEY_AZAN_SOUND = "azan_sound"
    const val KEY_REMINDER_MINUTES = "reminder_minutes"
    const val KEY_SILENT_MODE_RESPECT = "silent_mode_respect"
    const val KEY_LATITUDE = "latitude"
    const val KEY_LONGITUDE = "longitude"
    const val KEY_CITY_NAME = "city_name"
    const val KEY_COUNTRY_NAME = "country_name"

    // Prayer Enable/Disable Keys
    const val KEY_FAJR_ENABLED = "fajr_enabled"
    const val KEY_DHUHR_ENABLED = "dhuhr_enabled"
    const val KEY_ASR_ENABLED = "asr_enabled"
    const val KEY_MAGHRIB_ENABLED = "maghrib_enabled"
    const val KEY_ISHA_ENABLED = "isha_enabled"

    // Default Values
    const val DEFAULT_CALCULATION_METHOD = "MWL" // Muslim World League
    const val DEFAULT_REMINDER_MINUTES = 10

    // Qibla Direction (Kaaba coordinates)
    const val KAABA_LATITUDE = 21.4225
    const val KAABA_LONGITUDE = 39.8262
}