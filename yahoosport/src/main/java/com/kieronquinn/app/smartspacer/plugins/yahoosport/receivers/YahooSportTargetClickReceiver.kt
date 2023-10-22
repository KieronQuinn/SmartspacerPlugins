package com.kieronquinn.app.smartspacer.plugins.yahoosport.receivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applySecurity
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.plugins.yahoosport.widgets.YahooSportWidget
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID

class YahooSportTargetClickReceiver: BroadcastReceiver() {

    companion object {
        fun createPendingIntent(context: Context, smartspacerId: String): PendingIntent {
            val intent = Intent(context, YahooSportTargetClickReceiver::class.java)
            intent.applySecurity(context)
            intent.putExtra(EXTRA_SMARTSPACER_ID, smartspacerId)
            return PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent_MUTABLE_FLAGS
            )
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val smartspacerId = intent.getStringExtra(EXTRA_SMARTSPACER_ID) ?: return
        YahooSportWidget.clickWidget(context, smartspacerId)
    }

}