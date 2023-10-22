package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.HeadToHead.HeadToHeadOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultSubtitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTargetExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.TargetOptionsListener

class HeadToHead: TargetOptionsProvider<TargetTemplate.HeadToHead, HeadToHeadOptionsListener> {

    override fun getOptions(
        context: Context,
        template: TargetTemplate.HeadToHead,
        listener: HeadToHeadOptionsListener
    ): List<BaseSettingsItem> {
        return listOf(
            Setting(
                context.getString(R.string.configuration_target_head_to_head_main_title),
                context.getString(
                    R.string.configuration_target_head_to_head_main_content,
                    template.headToHeadTitle.describe()
                ),
                null,
                onClick = listener::onHeadToHeadTitleClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_head_to_head_first_text_title),
                template.headToHeadFirstCompetitorText.describe(),
                null,
                onClick = listener::onHeadToHeadFirstTeamNameClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_head_to_head_first_icon_title),
                template.headToHeadFirstCompetitorIcon.describe(context),
                null,
                onClick = listener::onHeadToHeadFirstTeamIconClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_head_to_head_second_text_title),
                template.headToHeadSecondCompetitorText.describe(),
                null,
                onClick = listener::onHeadToHeadSecondTeamNameClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_head_to_head_second_icon_title),
                template.headToHeadSecondCompetitorIcon.describe(context),
                null,
                onClick = listener::onHeadToHeadSecondTeamIconClicked
            )
        )
    }

    override fun getLabel(context: Context): String {
        return context.getString(R.string.configuration_target_head_to_head_title)
    }

    override fun createBlank(context: Context): TargetTemplate.HeadToHead {
        return TargetTemplate.HeadToHead(
            defaultTitle(context),
            defaultSubtitle(context),
            defaultIcon(),
            defaultClickAction(context),
            defaultTargetExtras(),
            Text(context.getString(R.string.configuration_target_head_to_head_main_content_default)),
            defaultIcon(),
            Text(context.getString(R.string.configuration_target_head_to_head_first_text_content)),
            defaultIcon(),
            Text(context.getString(R.string.configuration_target_head_to_head_second_text_content)),
        )
    }

    interface HeadToHeadOptionsListener: TargetOptionsListener {
        fun onHeadToHeadTitleClicked()
        fun onHeadToHeadFirstTeamNameClicked()
        fun onHeadToHeadFirstTeamIconClicked()
        fun onHeadToHeadSecondTeamNameClicked()
        fun onHeadToHeadSecondTeamIconClicked()
    }

}