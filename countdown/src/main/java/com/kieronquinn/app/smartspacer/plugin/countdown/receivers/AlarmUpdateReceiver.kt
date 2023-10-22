package com.kieronquinn.app.smartspacer.plugin.countdown.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.countdown.complications.CountdownComplication
import com.kieronquinn.app.smartspacer.plugin.countdown.repositories.AlarmRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AlarmUpdateReceiver: BroadcastReceiver(), KoinComponent {

    private val alarmRepository by inject<AlarmRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        SmartspacerComplicationProvider.notifyChange(context, CountdownComplication::class.java)
        alarmRepository.enqueueMidnightAlarm()
    }

}