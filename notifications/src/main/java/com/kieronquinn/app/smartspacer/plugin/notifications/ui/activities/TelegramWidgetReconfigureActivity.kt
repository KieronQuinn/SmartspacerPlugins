package com.kieronquinn.app.smartspacer.plugin.notifications.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider.Companion.EXTRA_SMARTSPACER_ID
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider

class TelegramWidgetReconfigureActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val smartspacerId = intent.getStringExtra(EXTRA_SMARTSPACER_ID)!!
        val sender =
            SmartspacerWidgetProvider.getReconfigureIntentSender(this, smartspacerId)!!
        startIntentSender(sender, null, 0, 0, 0)
        finish()
    }

}