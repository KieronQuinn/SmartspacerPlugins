package com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates

import android.content.ComponentName
import android.content.Context
import android.service.controls.Control
import android.service.controls.actions.BooleanAction
import android.service.controls.actions.FloatAction
import android.service.controls.templates.ToggleRangeTemplate
import androidx.core.content.ContextCompat
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData.RequirementType
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData.RequirementValueType
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting

/**
 *  As [ToggleTemplate], but also has a ranged value to show, which is delegated to [RangeTemplate]
 */
object ToggleRangeTemplate: ControlsTemplate<ToggleRangeTemplate>() {

    override fun getContent(
        context: Context,
        controlTemplate: ToggleRangeTemplate,
        control: Control,
        controlExtraData: ControlExtraData
    ): String {
        return if(controlTemplate.isChecked && !controlExtraData.shouldHideDetails) {
            RangeTemplate.getContent(context, controlTemplate.range, control, controlExtraData)
        }else{
            super.getContent(context, controlTemplate, control, controlExtraData)
        }
    }

    override fun getAvailableControlTapActions(): List<ControlTapAction> {
        return super.getAvailableControlTapActions() +
                listOf(ControlTapAction.BOOLEAN, ControlTapAction.FLOAT)
    }

    override fun getAvailableRequirementTypes(): List<RequirementType> {
        return super.getAvailableRequirementTypes() +
                listOf(RequirementType.BOOLEAN, RequirementType.FLOAT)
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
                val isChecked = (control.controlTemplate as? ToggleRangeTemplate)?.isChecked
                if(isChecked != null) {
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
                }else{
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
            ControlTapAction.FLOAT -> {
                val value = controlExtraData.floatSetFloat
                if(value != null) {
                    val action = FloatAction(control.controlTemplate.templateId, value)
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
                }else{
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

    override fun getExtraOptions(
        context: Context,
        control: Control,
        tapAction: ControlTapAction,
        actionData: ControlExtraData,
        interactions: ExtraOptionsInteractions
    ): List<BaseSettingsItem> {
        val template = (control.controlTemplate as? ToggleRangeTemplate)?.range
            ?: return emptyList()
        return listOf(
            SwitchSetting(
                actionData.shouldHideDetails,
                context.getString(R.string.configuration_hide_details_title),
                context.getString(R.string.configuration_hide_details_content),
                ContextCompat.getDrawable(context, R.drawable.ic_configuration_hide_details),
                onChanged = interactions.onHideDetailsChanged
            )
        ) + when(tapAction) {
            ControlTapAction.FLOAT -> {
                template.getSliderSetting(
                    context,
                    actionData.floatSetFloat ?: template.currentValue,
                    interactions.onFloatSet
                )
            }
            else -> null
        }.let {
            listOfNotNull(it)
        }
    }

    override fun getExtraRequirementOptions(
        context: Context,
        control: Control,
        requirementData: ControlsRequirement.RequirementData,
        interactions: ExtraRequirementOptionsInteractions
    ): List<BaseSettingsItem> {
        val template = (control.controlTemplate as? ToggleRangeTemplate)?.range
            ?: return emptyList()
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
            RequirementType.FLOAT -> {
                val current = requirementData.floatType ?: RequirementValueType.EQUALS
                listOf(
                    GenericSettingsItem.Dropdown(
                        context.getString(R.string.requirement_type_float_type_title),
                        context.getString(
                            R.string.requirement_type_float_type_content,
                            context.getString(current.label)
                        ),
                        ContextCompat.getDrawable(context, R.drawable.ic_configuration_float_type),
                        requirementData.floatType ?: RequirementValueType.EQUALS,
                        interactions.onValueTypeSet,
                        RequirementValueType.entries.toList(),
                    ) {
                        it.label
                    },
                    template.getSliderRequirementSetting(
                        context,
                        requirementData.float ?: template.currentValue,
                        interactions.onFloatSet
                    )
                )
            }
            else -> super.getExtraRequirementOptions(context, control, requirementData, interactions)
        }
    }

}