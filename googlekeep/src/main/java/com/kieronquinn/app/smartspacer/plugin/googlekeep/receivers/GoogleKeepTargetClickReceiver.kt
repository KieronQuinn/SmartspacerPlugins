package com.kieronquinn.app.smartspacer.plugin.googlekeep.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.googlekeep.widgets.GoogleKeepWidget.Companion.IDENTIFIER_OPEN_NOTE
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applySecurity
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider

class GoogleKeepTargetClickReceiver: BroadcastReceiver() {

    companion object {
        private const val KEY_SMARTSPACER_ID = "smartspacer_id"

        fun createIntent(context: Context, smartspacerId: String): Intent {
            return Intent(context, GoogleKeepTargetClickReceiver::class.java).apply {
                putExtra(KEY_SMARTSPACER_ID, smartspacerId)
                applySecurity(context)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val smartspacerId = intent.getStringExtra(KEY_SMARTSPACER_ID) ?: return
        SmartspacerWidgetProvider.clickView(context, smartspacerId, IDENTIFIER_OPEN_NOTE)
    }

}