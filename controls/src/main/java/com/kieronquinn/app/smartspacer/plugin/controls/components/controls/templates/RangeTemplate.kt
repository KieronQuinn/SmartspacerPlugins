package com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates

import android.content.ComponentName
import android.content.Context
import android.service.controls.Control
import android.service.controls.actions.FloatAction
import android.service.controls.templates.RangeTemplate
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
 *  Seek-style template, returns the current value
 */
object RangeTemplate: ControlsTemplate<RangeTemplate>() {

    override fun getContent(
        context: Context,
        controlTemplate: RangeTemplate,
        control: Control,
        controlExtraData: ControlExtraData
    ): String {
        return if(!controlExtraData.shouldHideDetails) {
            context.getString(
                R.string.control_template_two_part,
                super.getContent(context, controlTemplate, control, controlExtraData),
                controlTemplate.getValue()
            )
        }else{
            super.getContent(context, controlTemplate, control, controlExtraData)
        }
    }

    private fun RangeTemplate.getValue(): String {
        return String.format(formatString.toString(), currentValue)
    }

    override fun getAvailableControlTapActions(): List<ControlTapAction> {
        return super.getAvailableControlTapActions() + listOf(ControlTapAction.FLOAT)
    }

    override fun getAvailableRequirementTypes(): List<RequirementType> {
        return super.getAvailableRequirementTypes() + listOf(RequirementType.FLOAT)
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
        val template = control.controlTemplate as? RangeTemplate ?: return emptyList()
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
        val template = control.controlTemplate as? RangeTemplate ?: return emptyList()
        return when(requirementData.controlRequirementType) {
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