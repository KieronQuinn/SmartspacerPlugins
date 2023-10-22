package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider.ComplicationOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Basic.BasicOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultSubtitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTargetExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.TargetOptionsListener
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget

class Basic: TargetOptionsProvider<TargetTemplate.Basic, BasicOptionsListener> {

    override fun getOptions(
        context: Context,
        template: TargetTemplate.Basic,
        listener: BasicOptionsListener
    ): List<BaseSettingsItem> {
        return listOf(
            SwitchSetting(
                template.subComplication != null,
                context.getString(R.string.configuration_target_basic_sub_complication_title),
                context.getString(R.string.configuration_target_basic_sub_complication_content),
                icon = null,
                onChanged = listener::onTargetSubComplicationEnabledChanged
            ),
            *template.subComplication?.let {
                val provider = ComplicationOptionsProvider.getProvider(it::class.java)
                provider.getOptionsWithCast(
                    context,
                    it,
                    listener,
                    refreshPeriod = "",
                    refreshWhenNotVisible = false,
                    showHeader = false,
                    showExtras = false
                ).toTypedArray()
            } ?: emptyArray()
        )
    }

    override fun getLabel(context: Context): String {
        return context.getString(R.string.configuration_target_basic_title)
    }

    override fun createBlank(context: Context): TargetTemplate.Basic {
        return TargetTemplate.Basic(
            title = defaultTitle(context),
            subtitle = defaultSubtitle(context),
            icon = defaultIcon(),
            onClick = defaultClickAction(context),
            _featureType = SmartspaceTarget.FEATURE_UNDEFINED.toString(),
            targetExtras = defaultTargetExtras()
        )
    }

    interface BasicOptionsListener: TargetOptionsListener, ComplicationOptionsListener {
        fun onTargetSubComplicationEnabledChanged(enabled: Boolean)
    }

}