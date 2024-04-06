package com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates

import android.content.ComponentName
import android.content.Context
import android.service.controls.Control
import android.service.controls.actions.BooleanAction
import android.service.controls.templates.ToggleTemplate
import androidx.core.content.ContextCompat
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData.RequirementType
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting

/**
 *  On/off template, state is provided in regular content.
 */
object ToggleTemplate: ControlsTemplate<ToggleTemplate>() {

    override fun getAvailableControlTapActions(): List<ControlTapAction> {
        return super.getAvailableControlTapActions() + listOf(ControlTapAction.BOOLEAN)
    }

    override fun getAvailableRequirementTypes(): List<RequirementType> {
        return super.getAvailableRequirementTypes() + listOf(RequirementType.BOOLEAN)
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
            ControlTapAction.BOOLEAN -> {
                val isChecked = (control.controlTemplate as? ToggleTemplate)?.isChecked
                if (isChecked != null) {
                    val action = BooleanAction(control.controlTemplate.templateId, !isChecked)
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
                } else {
                    runFallbackAction(
                        context,
                        control,
                        componentName,
                        controlExtraData,
                        smartspacerId,
                        callback
                    )
                }
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

    override fun getExtraRequirementOptions(
        context: Context,
        control: Control,
        requirementData: ControlsRequirement.RequirementData,
        interactions: ExtraRequirementOptionsInteractions
    ): List<BaseSettingsItem> {
        return when(requirementData.controlRequirementType) {
            RequirementType.BOOLEAN -> {
                listOf(
                    SwitchSetting(
                        requirementData.boolean ?: false,
                        context.getString(R.string.requirement_type_boolean_title),
                        context.getString(R.string.requirement_type_boolean_content),
                        ContextCompat.getDrawable(context, R.drawable.ic_configuration_boolean),
                        onChanged = interactions.onBooleanSet
                    )
                )
            }
            else -> super.getExtraRequirementOptions(context, control, requirementData, interactions)
        }
    }

}