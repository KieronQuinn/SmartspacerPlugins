package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options

import android.content.Context
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.model.IconFont
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider.ComplicationOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.UiSurface_validSurfaces
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.describe
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.niceName
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.takeIfNotBlank
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import com.kieronquinn.app.smartspacer.sdk.model.UiSurface
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial

interface ComplicationOptionsProvider<T: ComplicationTemplate, L: ComplicationOptionsListener> {

    companion object {
        fun getProviderForTemplate(template: ComplicationTemplate): ComplicationOptionsProvider<out ComplicationTemplate, out ComplicationOptionsListener> {
            return getProvider(template::class.java)
        }

        inline fun <reified T: ComplicationTemplate> getProvider(): ComplicationOptionsProvider<out ComplicationTemplate, out ComplicationOptionsListener> {
            return getProvider(T::class.java)
        }

        fun getProvider(clazz: Class<out ComplicationTemplate>): ComplicationOptionsProvider<out ComplicationTemplate, out ComplicationOptionsListener> {
            return when(clazz) {
                ComplicationTemplate.Basic::class.java -> Basic()
                else -> throw RuntimeException("Unknown ComplicationTemplate ${clazz.simpleName}")
            }
        }

        fun defaultContent(context: Context): Text {
            return Text(context.getString(R.string.complication_initial_content))
        }

        fun defaultIcon(): Icon {
            val icon = CommunityMaterial.Icon.cmd_android
            return Icon.Font(
                _shouldTint = "true",
                contentDescription = icon.niceName,
                iconFontName = IconFont.COMMUNITY_MATERIAL.name,
                iconName = icon.name
            )
        }

        fun defaultImage(context: Context): Icon {
            return Icon.createDefaultImage(context)
        }

        fun defaultClickAction(context: Context): TapAction {
            return TapAction.LaunchApp.fromPackageName(
                context, SmartspacerConstants.SMARTSPACER_PACKAGE_NAME
            )
        }

        fun defaultComplicationExtras(): ComplicationTemplate.ComplicationExtras {
            return ComplicationTemplate.ComplicationExtras()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getOptionsWithCast(
        context: Context,
        template: ComplicationTemplate,
        listener: ComplicationOptionsListener,
        refreshPeriod: String,
        refreshWhenNotVisible: Boolean,
        showHeader: Boolean = true,
        showExtras: Boolean = true
    ): List<BaseSettingsItem> {
        val options = getOptions(context, template as T, listener as L)
        return listOfNotNull(
            Header(context.getString(R.string.configuration_complication_header))
                .takeIf { showHeader },
            Setting(
                context.getString(R.string.configuration_complication_icon_title),
                template.icon?.describe(context)
                    ?: context.getString(R.string.configuration_icon_none_title),
                null,
                onClick = listener::onComplicationIconClicked
            ),
            Setting(
                context.getString(R.string.configuration_complication_content_title),
                template.content.describe(),
                null,
                onClick = listener::onComplicationContentClicked
            ),
            SwitchSetting(
                template.disableTrim,
                context.getString(R.string.configuration_complication_disable_trim_title),
                context.getText(R.string.configuration_complication_disable_trim_content),
                null,
                onChanged = listener::onComplicationDisableTrimChanged
            ),
            Setting(
                context.getString(R.string.configuration_complication_tap_action_title),
                template.onClick.describe(context),
                null,
                onClick = listener::onComplicationTapActionClicked
            ),
            Setting(
                context.getString(R.string.configuration_complication_refresh_period_title),
                refreshPeriod.getRefreshPeriodContent(context),
                null,
                onClick = listener::onComplicationRefreshPeriodClicked
            ).takeIf { showExtras },
            SwitchSetting(
                refreshWhenNotVisible,
                context.getString(R.string.configuration_target_refresh_when_not_visible_title),
                context.getString(R.string.configuration_complication_refresh_when_not_visible_content),
                icon = null,
                onChanged = listener::onComplicationRefreshIfNotVisibleChanged
            ).takeIf { showExtras },
            Header(context.getString(R.string.configuration_complication_extras_header))
                .takeIf { showExtras },
            Setting(
                context.getString(R.string.configuration_complication_limit_to_surface_title),
                template.complicationExtras.limitToSurfaces.getLimitToContent(context)
                    ?: context.getText(R.string.configuration_complication_limit_to_surface_content),
                icon = null,
                onClick = listener::onComplicationLimitToSurfacesClicked
            ).takeIf { showExtras },
            Setting(
                context.getString(R.string.configuration_complication_weather_data_title),
                context.getString(R.string.configuration_complication_weather_data_content),
                icon = null,
                onClick = listener::onComplicationWeatherDataClicked
            ).takeIf { showExtras && Build.VERSION.SDK_INT >= 34 }
        ) + options
    }

    private fun Set<UiSurface>.getLimitToContent(context: Context): String? {
        //If all are set, setting is effectively disabled
        if(this == UiSurface_validSurfaces().toSet()) return null
        return joinToString(", ") {
            it.describe(context) ?: ""
        }.takeIfNotBlank()?.let {
            context.getString(R.string.configuration_complication_limit_to_surfaces_content_active, it)
        }
    }

    private fun String.getRefreshPeriodContent(context: Context): String {
        return if(isEmpty() || this == "0"){
            context.getString(R.string.configuration_target_refresh_period_content_unset)
        }else {
            val quantity = toIntOrNull() ?: 0
            context.resources.getQuantityString(
                R.plurals.configuration_target_refresh_period_content_set, quantity, this
            )
        }
    }

    fun getOptions(context: Context, template: T, listener: L): List<BaseSettingsItem>
    fun getLabel(context: Context): String
    fun createBlank(context: Context): T

    interface ComplicationOptionsListener {
        fun onComplicationIconClicked()
        fun onComplicationContentClicked()
        fun onComplicationTapActionClicked()
        fun onComplicationLimitToSurfacesClicked()
        fun onComplicationWeatherDataClicked()
        fun onComplicationRefreshPeriodClicked()
        fun onComplicationRefreshIfNotVisibleChanged(enabled: Boolean)
        fun onComplicationDisableTrimChanged(enabled: Boolean)
    }

}