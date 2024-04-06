package com.kieronquinn.app.smartspacer.plugin.controls.targets

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.service.controls.Control
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.controls.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates.ControlsTemplate
import com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates.StatelessTemplate.getIcon
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlMode
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.model.LoadingConfig
import com.kieronquinn.app.smartspacer.plugin.controls.providers.FontIconProvider
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlState
import com.kieronquinn.app.smartspacer.plugin.controls.service.ControlsForegroundService
import com.kieronquinn.app.smartspacer.plugin.controls.targets.ControlsTarget.TargetData.IconConfig
import com.kieronquinn.app.smartspacer.plugin.controls.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.plugin.controls.model.Icon as ControlIcon

class ControlsTarget: SmartspacerTargetProvider() {

    private val controlsRepository by inject<ControlsRepository>()
    private val dataRepository by inject<DataRepository>()
    private val gson by inject<Gson>()

    private val defaultIcon by lazy {
        AndroidIcon.createWithResource(provideContext(), R.drawable.ic_controls)
    }

    private val defaultTitle by lazy {
        resources.getString(R.string.target_title_default)
    }

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        ControlsForegroundService.startIfNeeded(provideContext())
        val data = getTargetData(smartspacerId)
        val componentName = data.componentName
        if(data.controlId == null || componentName == null) return emptyList()
        val control = controlsRepository.getControl(componentName, data.controlId, smartspacerId)
            ?: return emptyList()
        return listOfNotNull(control.toTarget(smartspacerId, componentName, data))
    }

    private fun ControlState.toTarget(
        smartspacerId: String,
        componentName: ComponentName,
        data: TargetData
    ): SmartspaceTarget? {
        return when(this) {
            is ControlState.Control -> {
                val template = ControlsTemplate.getTemplate(control.controlTemplate)
                template.getTarget(
                    control.controlTemplate,
                    provideContext(),
                    componentName,
                    control,
                    data
                )
            }
            is ControlState.Loading -> {
                TargetTemplate.Basic(
                    controlId.getTargetId(),
                    componentName,
                    SmartspaceTarget.FEATURE_UNDEFINED,
                    Text(cachedTitle ?: defaultTitle),
                    Text(resources.getString(R.string.control_loading)),
                    data.getIcon(IconConfig.Icon(cachedIcon ?: defaultIcon), provideContext()),
                    onClick = TapAction(intent = getSettingsIntent(smartspacerId))
                ).create()
            }
            is ControlState.Sending -> {
                TargetTemplate.Basic(
                    controlId.getTargetId(),
                    componentName,
                    SmartspaceTarget.FEATURE_UNDEFINED,
                    Text(cachedTitle ?: defaultTitle),
                    Text(resources.getString(R.string.control_sending)),
                    data.getIcon(IconConfig.Icon(cachedIcon ?: defaultIcon), provideContext()),
                    onClick = TapAction(intent = getSettingsIntent(smartspacerId))
                ).create()
            }
            is ControlState.Error -> {
                TargetTemplate.Basic(
                    controlId.getTargetId(),
                    componentName,
                    SmartspaceTarget.FEATURE_UNDEFINED,
                    Text(cachedTitle ?: defaultTitle),
                    Text(resources.getString(R.string.control_error)),
                    data.getIcon(IconConfig.Icon(cachedIcon ?: defaultIcon), provideContext()),
                    onClick = TapAction(intent = getSettingsIntent(smartspacerId))
                ).create()
            }
            is ControlState.Hidden -> null
        }?.apply {
            canBeDismissed = false
        }
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        return false //Cannot be dismissed
    }

    private fun String.getTargetId(): String {
        return "control_${this}_at_${System.currentTimeMillis()}"
    }

    private fun getSettingsIntent(smartspacerId: String): Intent {
        return createIntent(provideContext(), NavGraphMapping.CONFIGURATION_TARGET).apply {
            putExtra(SmartspacerConstants.EXTRA_SMARTSPACER_ID, smartspacerId)
        }
    }

    override fun onProviderRemoved(smartspacerId: String) {
        super.onProviderRemoved(smartspacerId)
        dataRepository.deleteTargetData(smartspacerId)
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            resources.getString(R.string.target_label),
            getDescription(smartspacerId),
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_controls),
            configActivity = createIntent(provideContext(), NavGraphMapping.CONFIGURATION_TARGET),
            setupActivity = createIntent(provideContext(), NavGraphMapping.CONFIGURATION_TARGET),
            broadcastProvider = "${BuildConfig.APPLICATION_ID}.broadcast.screenonoffunlock",
            allowAddingMoreThanOnce = true,
            refreshIfNotVisible = true
        )
    }

    private fun getDescription(smartspacerId: String?): String {
        val data = smartspacerId?.let { getTargetData(it) }
        val controlName = data?.customTitle ?: data?.controlName
        val controlApp = data?.controlApp
        return if(controlName != null && controlApp != null) {
            resources.getString(R.string.target_description_set, controlName, controlApp)
        }else{
            resources.getString(R.string.target_description)
        }
    }

    override fun createBackup(smartspacerId: String): Backup {
        val targetData = dataRepository
            .getTargetData(smartspacerId, TargetData::class.java)
        val normalisedTargetData = targetData?.let {
            it.copy(customIcon = it.customIcon as? ControlIcon.Font) //Can't backup files
        }
        val json = gson.toJson(normalisedTargetData)
        return Backup(json, getDescription(smartspacerId))
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val data = try {
            gson.fromJson(backup.data, TargetData::class.java)
        }catch (e: Exception) {
            null
        } ?: return false
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE,
            ::onRestored
        ) {
            data
        }
        return false //Still check for permissions
    }

    private fun onRestored(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    private fun getTargetData(smartspacerId: String): TargetData {
        return dataRepository.getTargetData(smartspacerId, TargetData::class.java)
            ?: TargetData(smartspacerId)
    }

    data class TargetData(
        @SerializedName("smartspacer_id")
        val smartspacerId: String,
        @SerializedName("control_component_name")
        val controlComponentName: String? = null,
        @SerializedName("control_app")
        val controlApp: String? = null,
        @SerializedName("control_id")
        val controlId: String? = null,
        @SerializedName("control_name")
        val controlName: String? = null,
        @SerializedName("custom_title")
        val customTitle: String? = null,
        @SerializedName("custom_subtitle")
        val customSubtitle: String? = null,
        @SerializedName("custom_icon")
        val customIcon: ControlIcon? = null,
        @SerializedName("loading_config")
        val loadingConfig: LoadingConfig? = null,
        @SerializedName("tap_action")
        val tapAction: ControlTapAction? = null,
        @SerializedName("mode_set_mode")
        val modeSetMode: ControlMode? = null,
        @SerializedName("float_set_float")
        val floatSetFloat: Float? = null,
        @SerializedName("hide_details")
        val hideDetails: Boolean? = null,
        @SerializedName("requires_unlock")
        val requiresUnlock: Boolean? = null
    ) {

        val loadConfig
            get() = loadingConfig ?: LoadingConfig.CACHED

        val componentName
            get() = controlComponentName?.let { ComponentName.unflattenFromString(it) }

        val controlTapAction
            get() = tapAction ?: ControlTapAction.SHOW_CONTROL

        val shouldHideDetails
            get() = hideDetails ?: false

        fun equalsIgnoringFloat(other: Any?): Boolean {
            if(other !is TargetData) return false
            if(other.smartspacerId != smartspacerId) return false
            if(other.controlComponentName != controlComponentName) return false
            if(other.controlApp != controlApp) return false
            if(other.controlName != controlName) return false
            if(other.customIcon != customIcon) return false
            if(other.customTitle != customTitle) return false
            if(other.customSubtitle != customSubtitle) return false
            if(other.loadingConfig != loadingConfig) return false
            if(other.tapAction != tapAction) return false
            if(other.requiresUnlock != requiresUnlock) return false
            if(other.hideDetails != hideDetails) return false
            return other.modeSetMode == modeSetMode
        }

        fun doesRequireUnlock(control: Control): Boolean {
            return requiresUnlock ?: if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                control.isAuthRequired
            }else false
        }

        fun getIcon(
            iconConfig: IconConfig,
            context: Context
        ): Icon {
            val icon = customIcon?.let {
                val uri = when(it) {
                    is ControlIcon.File -> Uri.parse(it.uri)
                    is ControlIcon.Font -> FontIconProvider.createUri(it)
                }
                AndroidIcon.createWithContentUri(uri)
            } ?: when(iconConfig) {
                is IconConfig.Control -> {
                    iconConfig.control.getIcon(context, iconConfig.componentName)
                }
                is IconConfig.Icon -> iconConfig.icon
            }
            val shouldTint = (customIcon as? ControlIcon.File)?.tint ?: true
            return Icon(icon, shouldTint = shouldTint)
        }

        sealed class IconConfig {
            data class Control(
                val control: android.service.controls.Control,
                val componentName: ComponentName
            ): IconConfig()
            data class Icon(val icon: AndroidIcon): IconConfig()
        }

        companion object {
            const val TYPE = "control"
        }
    }

}