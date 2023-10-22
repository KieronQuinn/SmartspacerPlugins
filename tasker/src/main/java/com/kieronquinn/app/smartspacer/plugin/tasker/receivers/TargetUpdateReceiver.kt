package com.kieronquinn.app.smartspacer.plugin.tasker.receivers

import android.content.Context
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetUpdateInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.TargetUpdateEventActivity
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerTargetUpdateReceiver

class TargetUpdateReceiver: SmartspacerTargetUpdateReceiver() {

    override fun onRequestSmartspaceTargetUpdate(
        context: Context,
        requestTargets: List<RequestTarget>
    ) {
        requestTargets.forEach {
            TargetUpdateEventActivity::class.java.requestQuery(
                context, SmartspacerTargetUpdateInput(it.smartspacerId)
            )
        }
    }

}