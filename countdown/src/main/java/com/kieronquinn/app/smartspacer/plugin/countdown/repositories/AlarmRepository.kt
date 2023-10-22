package com.kieronquinn.app.smartspacer.plugin.countdown.repositories

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.countdown.receivers.MidnightReceiver
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import java.time.LocalDate
import java.time.ZoneId

interface AlarmRepository {

    fun hasPermission(): Boolean
    fun enqueueMidnightAlarm()

}

class AlarmRepositoryImpl(private val context: Context): AlarmRepository {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun hasPermission(): Boolean {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return alarmManager.canScheduleExactAlarms()
    }

    override fun enqueueMidnightAlarm() {
        if(!hasPermission()) return
        val intent = MidnightReceiver.createIntent(context)
        val nextMidnight = LocalDate.now()
            .plusDays(1)
            .atStartOfDay()
            .atZone(ZoneId.systemDefault())
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC,
            nextMidnight.toInstant().toEpochMilli(),
            PendingIntent.getBroadcast(
                context,
                1,
                intent,
                PendingIntent_MUTABLE_FLAGS
            )
        )
    }

    init {
        enqueueMidnightAlarm()
    }

}