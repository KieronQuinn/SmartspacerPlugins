package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.receivers.SunriseRefreshReceiver
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.receivers.SunsetRefreshReceiver
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository.SunriseSunsetState
import com.kieronquinn.app.smartspacer.sdk.utils.applySecurity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.ZoneId

interface AlarmRepository {

    fun hasAlarmPermission(): Boolean
    fun scheduleNextSunriseAlarm()
    fun scheduleNextSunsetAlarm()

}

class AlarmRepositoryImpl(
    private val context: Context
): AlarmRepository, KoinComponent {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val sunriseSunsetRepository by inject<SunriseSunsetRepository>()

    override fun hasAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else true //Always granted on < S
    }

    override fun scheduleNextSunriseAlarm() {
        if(!hasAlarmPermission()) return
        val state = sunriseSunsetRepository.getSunriseState() ?: return
        val time = state.getNextTime() ?: return
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC,
            time,
            PendingIntent.getBroadcast(
                context,
                1,
                Intent(context, SunriseRefreshReceiver::class.java).apply {
                    applySecurity(context)
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }

    override fun scheduleNextSunsetAlarm() {
        if(!hasAlarmPermission()) return
        val state = sunriseSunsetRepository.getSunsetState() ?: return
        val time = state.getNextTime() ?: return
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC,
            time,
            PendingIntent.getBroadcast(
                context,
                2,
                Intent(context, SunsetRefreshReceiver::class.java).apply {
                    applySecurity(context)
                },
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        )
    }

    private fun SunriseSunsetState.getNextTime(): Long? {
        val show = timeToday?.let { it - showBefore.millis }
        val hide = timeToday?.let { it + showAfter.millis }
        val showTomorrow = timeTomorrow?.let { it - showBefore.millis }
        val hideTomorrow = timeTomorrow?.let { it + showAfter.millis }
        val fallback = LocalDate.now().plusDays(1).atStartOfDay()
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val now = System.currentTimeMillis()
        return listOfNotNull(
            show,
            hide,
            showTomorrow,
            hideTomorrow,
            fallback
        ).firstNotNullOfOrNull { time ->
            time.takeIf { it > now }
        }
    }

}