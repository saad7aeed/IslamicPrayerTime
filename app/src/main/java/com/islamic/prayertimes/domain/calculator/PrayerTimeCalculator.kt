package com.islamic.prayertimes.domain.calculator

import com.batoulapps.adhan.*
import com.batoulapps.adhan.data.DateComponents
import com.islamic.prayertimes.data.models.*
import java.text.SimpleDateFormat
import java.util.*

class PrayerTimeCalculator {

    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    fun calculatePrayerTimes(
        location: Location,
        date: Date,
        calculationMethod: PrayerCalculationMethod,
        settings: PrayerSettings
    ): DailyPrayerTimes {
        val coordinates = Coordinates(location.latitude, location.longitude)
        val params = getCalculationParameters(calculationMethod)

        // Convert Date to DateComponents
        val calendar = Calendar.getInstance().apply { time = date }
        val dateComponents = DateComponents(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,  // Calendar months are 0-based
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val prayerTimes = PrayerTimes(coordinates, dateComponents, params)

        return DailyPrayerTimes(
            date = date,
            fajr = PrayerTime(
                name = com.islamic.prayertimes.utils.Constants.FAJR,
                time = prayerTimes.fajr,
                timeString = timeFormatter.format(prayerTimes.fajr),
                isEnabled = settings.fajrEnabled
            ),
            dhuhr = PrayerTime(
                name = com.islamic.prayertimes.utils.Constants.DHUHR,
                time = prayerTimes.dhuhr,
                timeString = timeFormatter.format(prayerTimes.dhuhr),
                isEnabled = settings.dhuhrEnabled
            ),
            asr = PrayerTime(
                name = com.islamic.prayertimes.utils.Constants.ASR,
                time = prayerTimes.asr,
                timeString = timeFormatter.format(prayerTimes.asr),
                isEnabled = settings.asrEnabled
            ),
            maghrib = PrayerTime(
                name = com.islamic.prayertimes.utils.Constants.MAGHRIB,
                time = prayerTimes.maghrib,
                timeString = timeFormatter.format(prayerTimes.maghrib),
                isEnabled = settings.maghribEnabled
            ),
            isha = PrayerTime(
                name = com.islamic.prayertimes.utils.Constants.ISHA,
                time = prayerTimes.isha,
                timeString = timeFormatter.format(prayerTimes.isha),
                isEnabled = settings.ishaEnabled
            )
        )
    }

    private fun getCalculationParameters(method: PrayerCalculationMethod): CalculationParameters {
        return when (method) {
            PrayerCalculationMethod.MWL ->
                CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters

            PrayerCalculationMethod.ISNA ->
                com.batoulapps.adhan.CalculationMethod.NORTH_AMERICA.parameters

            PrayerCalculationMethod.EGYPT ->
                com.batoulapps.adhan.CalculationMethod.EGYPTIAN.parameters

            PrayerCalculationMethod.MAKKAH ->
                com.batoulapps.adhan.CalculationMethod.UMM_AL_QURA.parameters

            PrayerCalculationMethod.KARACHI ->
                com.batoulapps.adhan.CalculationMethod.KARACHI.parameters

            PrayerCalculationMethod.TEHRAN ->
                com.batoulapps.adhan.CalculationMethod.UMM_AL_QURA.parameters

            PrayerCalculationMethod.JAFARI -> createJafariParameters()
            PrayerCalculationMethod.DUBAI -> createDubaiParameters()
            PrayerCalculationMethod.KUWAIT -> createKuwaitParameters()
            PrayerCalculationMethod.QATAR -> createQatarParameters()
            PrayerCalculationMethod.SINGAPORE -> createSingaporeParameters()
            PrayerCalculationMethod.TURKEY -> createTurkeyParameters()
            PrayerCalculationMethod.FRANCE -> createFranceParameters()
            PrayerCalculationMethod.RUSSIA -> createRussiaParameters()
        }
    }

    private fun createJafariParameters(): CalculationParameters {
        val params = com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        params.fajrAngle = 16.0
        params.ishaAngle = 14.0
        params.madhab = Madhab.SHAFI
        return params
    }

    private fun createDubaiParameters(): CalculationParameters {
        val params = com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        params.fajrAngle = 18.2
        params.ishaAngle = 18.2
        params.madhab = Madhab.SHAFI
        return params
    }

    private fun createKuwaitParameters(): CalculationParameters {
        val params = com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        params.fajrAngle = 18.0
        params.ishaAngle = 17.5
        params.madhab = Madhab.SHAFI
        return params
    }

    private fun createQatarParameters(): CalculationParameters {
        val params = com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        params.fajrAngle = 18.0
        params.ishaInterval = 90
        params.madhab = Madhab.SHAFI
        return params
    }

    private fun createSingaporeParameters(): CalculationParameters {
        val params = com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        params.fajrAngle = 20.0
        params.ishaAngle = 18.0
        params.madhab = Madhab.SHAFI
        return params
    }

    private fun createTurkeyParameters(): CalculationParameters {
        val params = com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        params.fajrAngle = 18.0
        params.ishaAngle = 17.0
        params.madhab = Madhab.HANAFI
        return params
    }

    private fun createFranceParameters(): CalculationParameters {
        val params = com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        params.fajrAngle = 12.0
        params.ishaAngle = 12.0
        params.madhab = Madhab.SHAFI
        return params
    }

    private fun createRussiaParameters(): CalculationParameters {
        val params = com.batoulapps.adhan.CalculationMethod.MUSLIM_WORLD_LEAGUE.parameters
        params.fajrAngle = 16.0
        params.ishaAngle = 15.0
        params.madhab = Madhab.SHAFI
        return params
    }

    fun calculateQiblaDirection(userLocation: Location): Double {
        val userCoordinates = Coordinates(userLocation.latitude, userLocation.longitude)
        return Qibla(userCoordinates).direction
    }
}