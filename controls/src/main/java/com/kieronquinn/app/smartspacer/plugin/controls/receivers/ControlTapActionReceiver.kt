package com.kieronquinn.app.smartspacer.plugin.controls.receivers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.controls.Control
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.applySecurity
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getParcelableExtraCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getSerializableExtraCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ControlTapActionReceiver: BroadcastReceiver(), KoinComponent {

    companion object {
        private const val EXTRA_CONTROL = "control"
        private const val EXTRA_ACTION = "action"
        private const val EXTRA_ACTION_DATA = "action_data"
        private const val EXTRA_COMPONENT_NAME = "component_name"

        fun getIntent(
            context: Context,
            control: Control,
            action: ControlTapAction,
            controlExtraData: ControlExtraData,
            componentName: ComponentName,
            smartspacerId: String
        ): Intent {
            return Intent(context, ControlTapActionReceiver::class.java).apply {
                applySecurity(context)
                putExtra(EXTRA_CONTROL, control)
                putExtra(EXTRA_ACTION, action)
                putExtra(EXTRA_ACTION_DATA, controlExtraData)
                putExtra(EXTRA_COMPONENT_NAME, componentName)
                putExtra(EXTRA_SMARTSPACER_ID, smartspacerId)
            }
        }
    }

    private val controlsRepository by inject<ControlsRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        val control = intent.getParcelableExtraCompat(EXTRA_CONTROL, Control::class.java)
            ?: return
        val action = intent.getSerializableExtraCompat(EXTRA_ACTION, ControlTapAction::class.java)
            ?: return
        val actionData =
            intent.getParcelableExtraCompat(EXTRA_ACTION_DATA, ControlExtraData::class.java) ?: return
        val componentName = intent.getParcelableExtraCompat(
            EXTRA_COMPONENT_NAME, ComponentName::class.java
        ) ?: return
        val smartspacerId = intent.getStringExtra(EXTRA_SMARTSPACER_ID) ?: return
        controlsRepository.runControlTapAction(action, actionData, control, componentName, smartspacerId) { _, _, _ ->
            //We don't care about the result here
        }
    }

}