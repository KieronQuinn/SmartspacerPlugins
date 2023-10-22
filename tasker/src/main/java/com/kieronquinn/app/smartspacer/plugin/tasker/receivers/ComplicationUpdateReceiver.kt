package com.kieronquinn.app.smartspacer.plugin.tasker.receivers

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerComplicationUpdateInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.ComplicationUpdateEventActivity
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerComplicationUpdateReceiver

class ComplicationUpdateReceiver: SmartspacerComplicationUpdateReceiver() {

    override fun onRequestSmartspaceComplicationUpdate(
        context: Context,
        requestComplications: List<RequestComplication>
    ) {
        requestComplications.forEach {
            ComplicationUpdateEventActivity::class.java.requestQuery(
                context, SmartspacerComplicationUpdateInput(it.smartspacerId)
            )
        }
    }

}