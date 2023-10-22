package com.kieronquinn.app.smartspacer.plugin.energymonitor.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider.Companion.EXTRA_SMARTSPACER_ID
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider

class ReconfigureTrampolineActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val smartspacerId = intent.getStringExtra(EXTRA_SMARTSPACER_ID)!!
        val intentSender = SmartspacerWidgetProvider.getReconfigureIntentSender(
            this, smartspacerId
        )
        intentSender?.let {
            startIntentSender(intentSender, null, 0, 0, 0)
        }
        finish()
    }

}