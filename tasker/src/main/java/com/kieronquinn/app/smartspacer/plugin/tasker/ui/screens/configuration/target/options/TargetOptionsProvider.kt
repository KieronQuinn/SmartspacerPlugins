package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon.Companion.createDefaultImage
import com.kieronquinn.app.smartspacer.plugin.tasker.model.IconFont
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.TargetOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.DateTimeFormatter
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.DateTimeFormatter.Companion.format
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.UiSurface_validSurfaces
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.describe
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.niceName
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.takeIfNotBlank
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import com.kieronquinn.app.smartspacer.sdk.model.UiSurface
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import java.time.Instant

interface TargetOptionsProvider<T: TargetTemplate, L: TargetOptionsListener> {

    companion object {
        fun getProviderForTemplate(template: TargetTemplate): TargetOptionsProvider<out TargetTemplate, out TargetOptionsListener> {
            return getProvider(template::class.java)
        }

        inline fun <reified T: TargetTemplate> getProvider(): TargetOptionsProvider<out TargetTemplate, out TargetOptionsListener> {
           return getProvider(T::class.java)
        }

        fun getProvider(clazz: Class<out TargetTemplate>): TargetOptionsProvider<out TargetTemplate, out TargetOptionsListener> {
            return when(clazz) {
                TargetTemplate.Basic::class.java -> Basic()
                TargetTemplate.Button::class.java -> Button()
                TargetTemplate.Carousel::class.java -> Carousel()
                TargetTemplate.Doorbell::class.java -> Doorbell()
                TargetTemplate.HeadToHead::class.java -> HeadToHead()
                TargetTemplate.Image::class.java -> Image()
                TargetTemplate.Images::class.java -> Images()
                TargetTemplate.ListItems::class.java -> ListItems()
                TargetTemplate.LoyaltyCard::class.java -> LoyaltyCard()
                else -> throw RuntimeException("Unknown TargetTemplate ${clazz.simpleName}")
            }
        }

        fun defaultTitle(context: Context): Text {
            return Text(context.getString(R.string.target_initial_title))
        }

        fun defaultSubtitle(context: Context): Text {
            return Text(context.getString(R.string.target_initial_content))
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
            return createDefaultImage(context)
        }

        fun defaultClickAction(context: Context): TapAction {
            return TapAction.LaunchApp.fromPackageName(
                context, SmartspacerConstants.SMARTSPACER_PACKAGE_NAME
            )
        }

        fun defaultTargetExtras(): TargetTemplate.TargetExtras {
            return TargetTemplate.TargetExtras()
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getOptionsWithCast(
        context: Context,
        template: TargetTemplate,
        listener: TargetOptionsListener,
        dateTimeFormatter: DateTimeFormatter,
        refreshPeriod: String,
        refreshWhenNotVisible: Boolean
    ): List<BaseSettingsItem> {
        val options = getOptions(context, template as T, listener as L)
        return listOfNotNull(
            Header(context.getString(R.string.configuration_target_header)),
            Setting(
                context.getString(R.string.configuration_target_template_title),
                getLabel(context),
                null,
                onClick = listener::onTargetTemplateClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_title_title),
                template.title.describe(),
                null,
                onClick = listener::onTargetTitleClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_subtitle_title),
                template.subtitle.describe(),
                null,
                onClick = listener::onTargetSubtitleClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_icon_title),
                template.icon?.describe(context)
                    ?: context.getString(R.string.configuration_icon_none_title),
                null,
                onClick = listener::onTargetIconClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_tap_action_title),
                template.onClick.describe(context),
                null,
                onClick = listener::onTargetTapActionClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_refresh_period_title),
                refreshPeriod.getRefreshPeriodContent(context),
                null,
                onClick = listener::onTargetRefreshPeriodClicked
            ),
            SwitchSetting(
                refreshWhenNotVisible,
                context.getString(R.string.configuration_target_refresh_when_not_visible_title),
                context.getString(R.string.configuration_target_refresh_when_not_visible_content),
                icon = null,
                onChanged = listener::onTargetRefreshIfNotVisibleChanged
            ),
            *if(options.isNotEmpty()){
                arrayOf(
                    Header(getLabel(context)),
                    *options.toTypedArray()
                )
            }else emptyArray(),
            Header(context.getString(R.string.configuration_target_extras_header)),
            Setting(
                context.getString(R.string.configuration_target_expanded_state_title),
                context.getString(R.string.configuration_target_expanded_state_content),
                icon = null,
                onClick = listener::onTargetExpandedStateClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_source_notification_key_title),
                template.targetExtras.sourceNotificationKey
                    ?: context.getText(R.string.configuration_target_source_notification_key_content),
                icon = null,
                onClick = listener::onTargetSourceNotificationKeyClicked
            ),
            SwitchSetting(
                template.targetExtras.canBeDismissed,
                context.getString(R.string.configuration_target_can_be_dismissed_title),
                context.getString(R.string.configuration_target_can_be_dismissed_content),
                icon = null,
                onChanged = listener::onTargetAllowDismissChanged
            ),
            SwitchSetting(
                template.targetExtras.canTakeTwoComplications,
                context.getString(R.string.configuration_target_can_take_two_complications_title),
                context.getString(R.string.configuration_target_can_take_two_complications_content),
                icon = null,
                onChanged = listener::onTargetCanTakeTwoComplicationsChanged
            ).takeIf { template is TargetTemplate.Basic },
            SwitchSetting(
                template.targetExtras.hideIfNoComplications,
                context.getString(R.string.configuration_target_hide_if_no_complications_title),
                context.getText(R.string.configuration_target_hide_if_no_complications_content),
                icon = null,
                onChanged = listener::onTargetHideIfNoComplicationsChanged
            ),
            Setting(
                context.getString(R.string.configuration_target_limit_to_surfaces_title),
                template.targetExtras.limitToSurfaces.getLimitToContent(context)
                    ?: context.getText(R.string.configuration_target_limit_to_surfaces_content),
                icon = null,
                onClick = listener::onTargetLimitToSurfacesClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_about_intent_title),
                template.targetExtras.aboutIntent?.describe(context)
                    ?: context.getText(R.string.configuration_target_about_intent_content),
                icon = null,
                onClick = listener::onTargetAboutIntentClicked
            ).takeIf { template.supportsExtraBasedOptions },
            Setting(
                context.getString(R.string.configuration_target_feedback_intent_title),
                template.targetExtras.feedbackIntent?.describe(context)
                    ?: context.getText(R.string.configuration_target_feedback_intent_content),
                icon = null,
                onClick = listener::onTargetFeedbackIntentClicked
            ).takeIf { template.supportsExtraBasedOptions },
            SwitchSetting(
                template.targetExtras.hideTitleOnAod,
                context.getString(R.string.configuration_target_hide_title_on_aod_title),
                context.getString(R.string.configuration_target_hide_title_on_aod_content),
                icon = null,
                onChanged = listener::onTargetHideTitleOnAodChanged
            ).takeIf { template.supportsExtraBasedOptions },
            SwitchSetting(
                template.targetExtras.hideSubtitleOnAod,
                context.getString(R.string.configuration_target_hide_subtitle_on_aod_title),
                context.getString(R.string.configuration_target_hide_subtitle_on_aod_content),
                icon = null,
                onChanged = listener::onTargetHideSubtitleOnAodChanged
            ).takeIf { template.supportsExtraBasedOptions }
        )
    }

    fun getOptions(context: Context, template: T, listener: L): List<BaseSettingsItem>
    fun getLabel(context: Context): String
    fun createBlank(context: Context): T
    fun getConfig() = Config()

    private fun Set<UiSurface>.getLimitToContent(context: Context): String? {
        //If all are set, setting is effectively disabled
        if(this == UiSurface_validSurfaces().toSet()) return null
        return joinToString(", ") {
            it.describe(context) ?: ""
        }.takeIfNotBlank()?.let {
            context.getString(R.string.configuration_target_limit_to_surfaces_content_active, it)
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

    private fun Long.parseDateTime(dateTimeFormatter: DateTimeFormatter): String {
        return Instant.ofEpochMilli(this).format(dateTimeFormatter)
    }

    interface TargetOptionsListener {
        fun onTargetTemplateClicked()
        fun onTargetTitleClicked()
        fun onTargetSubtitleClicked()
        fun onTargetIconClicked()
        fun onTargetTapActionClicked()
        fun onTargetRefreshPeriodClicked()
        fun onTargetRefreshIfNotVisibleChanged(enabled: Boolean)
        fun onTargetExpandedStateClicked()
        fun onTargetSourceNotificationKeyClicked()
        fun onTargetAllowDismissChanged(enabled: Boolean)
        fun onTargetCanTakeTwoComplicationsChanged(enabled: Boolean)
        fun onTargetHideIfNoComplicationsChanged(enabled: Boolean)
        fun onTargetLimitToSurfacesClicked()
        fun onTargetAboutIntentClicked()
        fun onTargetFeedbackIntentClicked()
        fun onTargetHideTitleOnAodChanged(enabled: Boolean)
        fun onTargetHideSubtitleOnAodChanged(enabled: Boolean)
    }

    data class Config(
        val supportsBundleExtras: Boolean = true
    )

}