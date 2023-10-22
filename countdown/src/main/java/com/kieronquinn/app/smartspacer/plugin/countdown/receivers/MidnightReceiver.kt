package com.kieronquinn.app.smartspacer.plugin.countdown.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.countdown.complications.CountdownComplication
import com.kieronquinn.app.smartspacer.plugin.countdown.repositories.AlarmRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.applySecurity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MidnightReceiver: BroadcastReceiver(), KoinComponent {

    companion object {
        fun createIntent(context: Context): Intent {
            return Intent(context, MidnightReceiver::class.java).apply {
                applySecurity(context)
            }
        }
    }

    private val alarmRepository by inject<AlarmRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        SmartspacerComplicationProvider.notifyChange(context, CountdownComplication::class.java)
        alarmRepository.enqueueMidnightAlarm()
    }

}