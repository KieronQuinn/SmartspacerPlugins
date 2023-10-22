package com.kieronquinn.app.smartspacer.plugin.samsunghealth.receivers

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.complications.SleepComplication
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerComplicationUpdateReceiver

class SleepComplicationReceiver: SmartspacerComplicationUpdateReceiver() {

    override fun onRequestSmartspaceComplicationUpdate(
        context: Context,
        requestComplications: List<RequestComplication>
    ) {
        SmartspacerComplicationProvider.notifyChange(context, SleepComplication::class.java)
    }

}