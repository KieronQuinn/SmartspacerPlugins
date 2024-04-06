package com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.controls.Control
import android.service.controls.actions.ControlAction
import android.service.controls.templates.ControlTemplate
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.slider.LabelFormatter
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.complications.ControlsComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlMode
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.model.RenderInfo
import com.kieronquinn.app.smartspacer.plugin.controls.receivers.ControlTapActionReceiver
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData.RequirementType
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData.RequirementValueType
import com.kieronquinn.app.smartspacer.plugin.controls.targets.ControlsTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.popup.PopupControlDialogFragment
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.PendingIntent_MUTABLE_FLAGS
import com.kieronquinn.app.smartspacer.sdk.annotations.DisablingTrim
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import com.kieronquinn.app.smartspacer.sdk.utils.TrimToFit
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import android.graphics.drawable.Icon as AndroidIcon
import android.service.controls.templates.RangeTemplate as SystemRangeTemplate
import com.kieronquinn.app.smartspacer.plugin.controls.complications.ControlsComplication.ComplicationData.IconConfig as ComplicationIcon
import com.kieronquinn.app.smartspacer.plugin.controls.targets.ControlsTarget.TargetData.IconConfig as TargetIcon

abstract class ControlsTemplate<T: ControlTemplate>: KoinComponent {

    companion object {
        fun <T: ControlTemplate> getTemplate(template: T): ControlsTemplate<T> {
            return when(template.templateType) {
                ControlTemplate.TYPE_ERROR -> ErrorTemplate
                ControlTemplate.TYPE_NO_TEMPLATE -> NoTemplate
                ControlTemplate.TYPE_RANGE -> RangeTemplate
                ControlTemplate.TYPE_STATELESS -> StatelessTemplate
                ControlTemplate.TYPE_TEMPERATURE -> TemperatureControlTemplate
                ControlTemplate.TYPE_THUMBNAIL -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        ThumbnailTemplate
                    } else {
                        ErrorTemplate
                    }
                }
                ControlTemplate.TYPE_TOGGLE -> ToggleTemplate
                ControlTemplate.TYPE_TOGGLE_RANGE -> ToggleRangeTemplate
                else -> NoTemplate
            } as ControlsTemplate<T>
        }
    }

    protected val controlsRepository by inject<ControlsRepository>()

    @OptIn(DisablingTrim::class)
    open fun getComplication(
        template: T,
        context: Context,
        componentName: ComponentName,
        control: Control,
        complicationData: ComplicationData
    ): SmartspaceAction? {
        val controlExtraData = ControlExtraData(
            complicationData.doesRequireUnlock(control),
            complicationData.modeSetMode,
            complicationData.floatSetFloat,
            complicationData.shouldHideDetails
        )
        return ComplicationTemplate.Basic(
            getId(control, complicationData.hashCode()),
            complicationData.getIcon(ComplicationIcon.Control(control, componentName), context),
            Text(
                complicationData.customTitle
                    ?: getContent(context, template, control, controlExtraData)
            ),
            getTapAction(
                context,
                complicationData.controlTapAction,
                controlExtraData,
                control,
                componentName,
                complicationData.smartspacerId
            ),
            trimToFit = TrimToFit.Disabled
        ).create()
    }

    open fun getTarget(
        template: T,
        context: Context,
        componentName: ComponentName,
        control: Control,
        targetData: TargetData
    ): SmartspaceTarget? {
        val controlExtraData = ControlExtraData(
            targetData.doesRequireUnlock(control),
            targetData.modeSetMode,
            targetData.floatSetFloat,
            targetData.shouldHideDetails
        )
        return TargetTemplate.Basic(
            getId(control, targetData.hashCode()),
            componentName,
            SmartspaceTarget.FEATURE_UNDEFINED,
            Text(targetData.customTitle ?: control.title),
            Text(
                targetData.customSubtitle
                    ?: getContent(context, template, control, controlExtraData)
            ),
            targetData.getIcon(TargetIcon.Control(control, componentName), context),
            getTapAction(
                context,
                targetData.controlTapAction,
                controlExtraData,
                control,
                componentName,
                targetData.smartspacerId
            )
        ).create()
    }

    open fun Control.getIcon(context: Context, controlComponent: ComponentName): AndroidIcon {
        customIcon?.let { return it }
        val renderInfo = RenderInfo.lookup(context, controlComponent, deviceType)
        val bitmap = renderInfo.icon.toBitmap()
        return AndroidIcon.createWithBitmap(bitmap)
    }

    @CallSuper
    open fun getAvailableControlTapActions(): List<ControlTapAction> {
        return listOf(
            ControlTapAction.SHOW_CONTROL,
            ControlTapAction.OPEN_PANEL,
            ControlTapAction.OPEN_APP
        )
    }

    @CallSuper
    open fun getAvailableRequirementTypes(): List<RequirementType> {
        return listOf(RequirementType.AVAILABLE)
    }

    @CallSuper
    open fun invokeTapAction(
        context: Context,
        tapAction: ControlTapAction,
        controlExtraData: ControlExtraData,
        control: Control,
        componentName: ComponentName,
        smartspacerId: String,
        callback: (Int, ControlTapAction, ControlExtraData) -> Unit
    ) {
        when(tapAction) {
            ControlTapAction.SHOW_CONTROL -> {
                val intent = PopupControlDialogFragment.createLaunchIntent(
                    context,
                    control.controlId,
                    componentName,
                    smartspacerId,
                    controlExtraData.requiresUnlock
                )
                context.startActivity(intent)
            }
            ControlTapAction.OPEN_PANEL -> {
                controlsRepository.launchPanelIntent(componentName)
            }
            ControlTapAction.OPEN_APP -> {
                controlsRepository.launchAppPendingIntent(control.appIntent)
            }
            else -> {
                //No-op
            }
        }
    }

    open fun getId(control: Control, hash: Int? = null): String {
        return "control_${control.controlId}_at_${hash ?: System.currentTimeMillis()}"
    }

    open fun getContent(
        context: Context,
        controlTemplate: T,
        control: Control,
        controlExtraData: ControlExtraData
    ): String {
        return control.statusText.toString().ifBlank { control.title.toString() }
    }

    open fun getExtraOptions(
        context: Context,
        control: Control,
        tapAction: ControlTapAction,
        actionData: ControlExtraData,
        interactions: ExtraOptionsInteractions
    ): List<BaseSettingsItem> {
        return emptyList()
    }

    open fun getExtraRequirementOptions(
        context: Context,
        control: Control,
        requirementData: RequirementData,
        interactions: ExtraRequirementOptionsInteractions
    ): List<BaseSettingsItem> {
        return emptyList()
    }

    protected fun handleResult(
        context: Context,
        control: Control,
        componentName: ComponentName,
        controlExtraData: ControlExtraData,
        result: Int,
        smartspacerId: String,
        callback: (Int, ControlTapAction, ControlExtraData) -> Unit
    ) {
        if(result != ControlAction.RESPONSE_OK) {
            //Failed to run whatever the action was, show the control instead
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

    protected fun runFallbackAction(
        context: Context,
        control: Control,
        componentName: ComponentName,
        controlExtraData: ControlExtraData,
        smartspacerId: String,
        callback: (Int, ControlTapAction, ControlExtraData) -> Unit
    ) {
        invokeTapAction(
            context,
            ControlTapAction.SHOW_CONTROL,
            controlExtraData,
            control,
            componentName,
            smartspacerId,
            callback
        )
    }

    protected fun getTapAction(
        context: Context,
        controlTapAction: ControlTapAction,
        actionData: ControlExtraData,
        control: Control,
        componentName: ComponentName,
        smartspacerId: String
    ): TapAction {
        val id = getId(control)
        val intent = ControlTapActionReceiver.getIntent(
            context,
            control,
            controlTapAction,
            actionData,
            componentName,
            smartspacerId
        )
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent_MUTABLE_FLAGS
        )
        return TapAction(
            pendingIntent = pendingIntent,
            shouldShowOnLockScreen = !actionData.requiresUnlock
        )
    }

    protected fun SystemRangeTemplate.getSliderSetting(
        context: Context,
        current: Float,
        onSet: (Float) -> Unit
    ): GenericSettingsItem.Slider {
        return GenericSettingsItem.Slider(
            current,
            minValue,
            maxValue,
            stepValue,
            context.getString(R.string.configuration_float_set_title),
            context.getString(R.string.configuration_float_set_content),
            ContextCompat.getDrawable(context, R.drawable.ic_setting_float),
            { value -> String.format(formatString.toString(), value) },
            LabelFormatter.LABEL_VISIBLE,
            onSet
        )
    }

    protected fun SystemRangeTemplate.getSliderRequirementSetting(
        context: Context,
        current: Float,
        onSet: (Float) -> Unit
    ): GenericSettingsItem.Slider {
        return GenericSettingsItem.Slider(
            current,
            minValue,
            maxValue,
            stepValue,
            context.getString(R.string.requirement_type_float_title),
            context.getString(R.string.requirement_type_float_content),
            ContextCompat.getDrawable(context, R.drawable.ic_setting_float),
            { value -> String.format(formatString.toString(), value) },
            LabelFormatter.LABEL_VISIBLE,
            onSet
        )
    }

    data class ExtraOptionsInteractions(
        val onFloatSet: (Float) -> Unit,
        val onModeSet: (ControlMode) -> Unit,
        val onHideDetailsChanged: (Boolean) -> Unit
    )

    data class ExtraRequirementOptionsInteractions(
        val onBooleanSet: (Boolean) -> Unit,
        val onModeSet: (ControlMode) -> Unit,
        val onValueTypeSet: (RequirementValueType) -> Unit,
        val onFloatSet: (Float) -> Unit
    )

}