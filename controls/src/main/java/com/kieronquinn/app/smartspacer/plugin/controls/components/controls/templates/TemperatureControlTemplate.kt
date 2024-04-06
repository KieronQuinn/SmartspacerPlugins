package com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates

import android.content.ComponentName
import android.content.Context
import android.service.controls.Control
import android.service.controls.actions.ModeAction
import android.service.controls.templates.ControlTemplate
import android.service.controls.templates.TemperatureControlTemplate
import androidx.core.content.ContextCompat
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.complications.ControlsComplication
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlMode
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData.RequirementType
import com.kieronquinn.app.smartspacer.plugin.controls.targets.ControlsTarget
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget

object TemperatureControlTemplate: ControlsTemplate<TemperatureControlTemplate>() {

    override fun getComplication(
        template: TemperatureControlTemplate,
        context: Context,
        componentName: ComponentName,
        control: Control,
        complicationData: ControlsComplication.ComplicationData
    ): SmartspaceAction? {
        val subTemplate = template.template.takeIf {
            it != ControlTemplate.getErrorTemplate() && it != ControlTemplate.getNoTemplateObject()
        }
        return if(subTemplate != null) {
            getTemplate(subTemplate)
                .getComplication(subTemplate, context, componentName, control, complicationData)
        }else{
            super.getComplication(template, context, componentName, control, complicationData)
        }
    }

    override fun getTarget(
        template: TemperatureControlTemplate,
        context: Context,
        componentName: ComponentName,
        control: Control,
        targetData: ControlsTarget.TargetData
    ): SmartspaceTarget? {
        val subTemplate = template.template.takeIf {
            it != ControlTemplate.getErrorTemplate() && it != ControlTemplate.getNoTemplateObject()
        }
        return if(subTemplate != null) {
            getTemplate(subTemplate)
                .getTarget(subTemplate, context, componentName, control, targetData)
        }else{
            super.getTarget(template, context, componentName, control, targetData)
        }
    }

    override fun getAvailableControlTapActions(): List<ControlTapAction> {
        return super.getAvailableControlTapActions() + listOf(ControlTapAction.MODE_SET)
    }

    override fun getAvailableRequirementTypes(): List<RequirementType> {
        return super.getAvailableRequirementTypes() + listOf(RequirementType.MODE)
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
            ControlTapAction.MODE_SET -> {
                val mode = controlExtraData.modeSetMode?.mode
                if(mode != null) {
                    val action = ModeAction(control.controlTemplate.templateId, mode)
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

    override fun getContent(
        context: Context,
        controlTemplate: TemperatureControlTemplate,
        control: Control,
        controlExtraData: ControlExtraData
    ): String {
        val mode = when(controlTemplate.currentActiveMode) {
            TemperatureControlTemplate.MODE_COOL -> R.string.control_template_state_cool
            TemperatureControlTemplate.MODE_ECO -> R.string.control_template_state_eco
            TemperatureControlTemplate.MODE_HEAT -> R.string.control_template_state_heat
            TemperatureControlTemplate.MODE_HEAT_COOL -> R.string.control_template_state_heat_cool
            TemperatureControlTemplate.MODE_OFF -> R.string.control_template_state_off
            else -> null
        }
        return if(mode != null && !controlExtraData.shouldHideDetails) {
            context.getString(
                R.string.control_template_two_part,
                super.getContent(context, controlTemplate, control, controlExtraData),
                context.getString(mode)
            )
        }else{
            super.getContent(context, controlTemplate, control, controlExtraData)
        }
    }

    override fun getExtraOptions(
        context: Context,
        control: Control,
        tapAction: ControlTapAction,
        actionData: ControlExtraData,
        interactions: ExtraOptionsInteractions
    ): List<BaseSettingsItem> {
        val template = control.controlTemplate as? TemperatureControlTemplate ?: return emptyList()
        val current = actionData.modeSetMode ?: ControlMode.fromMode(template.currentMode)
            ?: ControlMode.MODE_OFF
        return listOf(
            SwitchSetting(
                actionData.shouldHideDetails,
                context.getString(R.string.configuration_hide_details_title),
                context.getString(R.string.configuration_hide_details_content),
                ContextCompat.getDrawable(context, R.drawable.ic_configuration_hide_details),
                onChanged = interactions.onHideDetailsChanged
            )
        ) + when(tapAction) {
            ControlTapAction.MODE_SET -> {
                getModeSetTemplate(
                    context,
                    current,
                    template.getAvailableModes(),
                    interactions.onModeSet
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
        val currentMode = requirementData.mode ?: ControlMode.MODE_OFF
        return when(requirementData.controlRequirementType) {
            RequirementType.MODE -> {
                listOf(
                    Dropdown(
                        context.getString(R.string.requirement_type_mode_title),
                        context.getString(
                            R.string.requirement_type_mode_content,
                            context.getString(currentMode.label)
                        ),
                        ContextCompat.getDrawable(context, R.drawable.ic_mode_set),
                        currentMode,
                        interactions.onModeSet,
                        ControlMode.entries.toList()
                    ) {
                        it.label
                    }
                )
            }
            else -> super.getExtraRequirementOptions(context, control, requirementData, interactions)
        }
    }

    private fun TemperatureControlTemplate.getAvailableModes(): List<ControlMode> {
        return ControlMode.entries.filter {
            modes and it.modeFlag != 0
        }
    }

    private fun getModeSetTemplate(
        context: Context,
        current: ControlMode,
        available: List<ControlMode>,
        onSet: (ControlMode) -> Unit
    ): Dropdown<*> {
        return Dropdown(
            context.getString(R.string.configuration_mode_set_title),
            context.getString(
                R.string.configuration_mode_set_content,
                context.getString(current.label)
            ),
            ContextCompat.getDrawable(context, R.drawable.ic_mode_set),
            current,
            onSet,
            available
        ) {
            it.label
        }
    }

}