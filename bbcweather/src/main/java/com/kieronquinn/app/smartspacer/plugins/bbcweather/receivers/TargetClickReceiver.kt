package com.kieronquinn.app.smartspacer.plugins.bbcweather.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applySecurity
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.plugins.bbcweather.widgets.BBCWeatherTargetWidget
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID

class TargetClickReceiver: BroadcastReceiver() {

    companion object {
        fun createIntent(context: Context, smartspacerId: String): Intent {
            return Intent(context, TargetClickReceiver::class.java).apply {
                applySecurity(context)
                putExtra(EXTRA_SMARTSPACER_ID, smartspacerId)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val smartspacerId = intent.getStringExtra(EXTRA_SMARTSPACER_ID) ?: return
        BBCWeatherTargetWidget.clickWidget(context, smartspacerId)
    }

}