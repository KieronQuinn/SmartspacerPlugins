package com.kieronquinn.app.smartspacer.plugin.tasker.ui.views

import android.content.ComponentName
import android.content.Context
import android.util.AttributeSet
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.sdk.client.views.BcSmartspaceView
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget

class DemoBcSmartspaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
): BcSmartspaceView(context, attrs) {

    companion object {
        const val TARGET_ID_PREVIEW = "preview"

        fun TargetTemplate.toDemoTarget(context: Context): SmartspaceTarget {
            val componentName = ComponentName(TARGET_ID_PREVIEW, TARGET_ID_PREVIEW)
            return toTarget(context, componentName, TARGET_ID_PREVIEW).apply {
                canBeDismissed = false
            }
        }

        fun ComplicationTemplate.toDemoComplication(context: Context): SmartspaceTarget {
            val componentName = ComponentName(TARGET_ID_PREVIEW, TARGET_ID_PREVIEW)
            val complication = toComplication(context, TARGET_ID_PREVIEW)
            return SmartspaceTarget(
                smartspaceTargetId = TARGET_ID_PREVIEW,
                featureType = SmartspaceTarget.FEATURE_WEATHER,
                componentName = componentName,
                headerAction = complication,
                baseAction = SmartspaceAction(id = "", title = "")
            )
        }
    }

    override fun onSmartspaceTargetsUpdate(targets: List<SmartspaceTarget>) {
        //Ignore changes which are not our preview targets
        if(!targets.all { it.smartspaceTargetId ==  TARGET_ID_PREVIEW}) return
        super.onSmartspaceTargetsUpdate(targets)
    }

    override fun onLongPress(target: SmartspaceTarget): Boolean {
        //Ignore long presses since the dialog is useless here
        return true
    }

}