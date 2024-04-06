package com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates

import android.content.ComponentName
import android.content.Context
import android.service.controls.Control
import android.service.controls.actions.CommandAction
import android.service.controls.templates.StatelessTemplate
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction

/**
 *  A template with basic content - no state changes.
 */
object StatelessTemplate: ControlsTemplate<StatelessTemplate>() {

    override fun getAvailableControlTapActions(): List<ControlTapAction> {
        return super.getAvailableControlTapActions() + listOf(ControlTapAction.COMMAND)
    }

    override fun invokeTapAction(
        context: Context,
        tapAction: ControlTapAction,
        controlExtraData: ControlExtraData,
        control: Control,
        componentName: ComponentName,
        smartspacerId: String,
        callback: (Int, ControlTapAction, ControlExtraData) -> Unit
    ) {
        when(tapAction) {
            ControlTapAction.COMMAND -> {
                val action = CommandAction(control.controlTemplate.templateId)
                controlsRepository.runControlAction(
                    control,
                    componentName,
                    action,
                    tapAction,
                    controlExtraData,
                    smartspacerId,
                    callback,
                    ::handleResult
                )
            }
            else -> super.invokeTapAction(
                context,
                tapAction,
                controlExtraData,
                control,
                componentName,
                smartspacerId,
                callback
            )
        }
    }

}