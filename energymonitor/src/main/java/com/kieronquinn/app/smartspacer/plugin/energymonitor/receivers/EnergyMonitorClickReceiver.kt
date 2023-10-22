package com.kieronquinn.app.smartspacer.plugin.energymonitor.receivers

import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.energymonitor.widgets.EnergyMonitorWidget

class EnergyMonitorClickReceiver: SecureBroadcastReceiver() {

    companion object {
        private const val EXTRA_SMARTSPACER_ID = "smartspacer_id"

        fun createIntent(context: Context, smartspacerId: String): Intent {
            val intent = Intent(context, EnergyMonitorClickReceiver::class.java)
            intent.putExtra(EXTRA_SMARTSPACER_ID, smartspacerId)
            putExtra(context, intent)
            return intent
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        val smartspacerId = intent?.getStringExtra(EXTRA_SMARTSPACER_ID) ?: return
        EnergyMonitorWidget.clickWidget(context, smartspacerId)
    }

}