package com.kieronquinn.app.smartspacer.plugins.datausage.receivers

import android.content.Context
import com.kieronquinn.app.smartspacer.plugins.datausage.complications.DataUsageComplication
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerComplicationUpdateReceiver

class DataUsageReceiver: SmartspacerComplicationUpdateReceiver() {

    override fun onRequestSmartspaceComplicationUpdate(
        context: Context,
        requestComplications: List<RequestComplication>
    ) {
        requestComplications.forEach {
            SmartspacerComplicationProvider.notifyChange(
                context, DataUsageComplication::class.java, it.smartspacerId
            )
        }
    }

}