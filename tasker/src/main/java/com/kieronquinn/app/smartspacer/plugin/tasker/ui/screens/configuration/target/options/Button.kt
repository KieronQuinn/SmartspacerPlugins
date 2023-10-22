package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Button.ButtonOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultSubtitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTargetExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.TargetOptionsListener

class Button: TargetOptionsProvider<TargetTemplate.Button, ButtonOptionsListener> {

    override fun getOptions(
        context: Context,
        template: TargetTemplate.Button,
        listener: ButtonOptionsListener
    ): List<BaseSettingsItem> {
        return listOf(
            Setting(
                context.getString(R.string.configuration_target_button_content_title),
                context.getString(
                    R.string.configuration_target_button_content_content,
                    template.buttonText.describe()
                ),
                null,
                onClick = listener::onButtonTitleClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_button_icon_title),
                template.buttonIcon.describe(context),
                null,
                onClick = listener::onButtonIconClicked
            )
        )
    }

    override fun getLabel(context: Context): String {
        return context.getString(R.string.configuration_target_button_title)
    }

    override fun createBlank(context: Context): TargetTemplate.Button {
        return TargetTemplate.Button(
            defaultTitle(context),
            defaultSubtitle(context),
            defaultIcon(),
            defaultClickAction(context),
            defaultTargetExtras(),
            defaultIcon(),
            Text(context.getString(R.string.configuration_target_button_content_content_default))
        )
    }

    interface ButtonOptionsListener: TargetOptionsListener {
        fun onButtonTitleClicked()
        fun onButtonIconClicked()
    }

}