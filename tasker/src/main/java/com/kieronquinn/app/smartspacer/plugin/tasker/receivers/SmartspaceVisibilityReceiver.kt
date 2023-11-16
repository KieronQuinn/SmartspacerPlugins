package com.kieronquinn.app.smartspacer.plugin.tasker.receivers

import android.content.Context
import android.util.Log
import com.joaomgcd.taskerpluginlibrary.extensions.requestQuery
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerSmartspaceVisibilityTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.SmartspaceVisibilityEventActivity
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerVisibilityChangedReceiver

class SmartspaceVisibilityReceiver: SmartspacerVisibilityChangedReceiver() {

    override fun onSmartspaceVisibilityChanged(context: Context, visible: Boolean, timestamp: Long) {
        Log.d("SVR", "Visibility: $visible")
        SmartspaceVisibilityEventActivity::class.java
            .requestQuery(context, SmartspacerSmartspaceVisibilityTaskerInput(visible))
    }

}