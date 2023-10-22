package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.ListItems.ListItemsOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultSubtitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTargetExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.TargetOptionsListener

class ListItems: TargetOptionsProvider<TargetTemplate.ListItems, ListItemsOptionsListener> {

    companion object {
        fun defaultListItem(context: Context, index: Int): Text {
            return Text(
                context.getString(R.string.configuration_target_list_items_content_default, index)
            )
        }
    }

    override fun getOptions(
        context: Context,
        template: TargetTemplate.ListItems,
        listener: ListItemsOptionsListener
    ): List<BaseSettingsItem> {
        return listOf(
            Setting(
                context.getString(R.string.configuration_target_list_items_title),
                template.listItems.describe(context),
                null,
                onClick = listener::onListItemsItemsClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_list_icon_title),
                template.listIcon.describe(context),
                null,
                onClick = listener::onListItemsIconClicked
            ),
            Setting(
                context.getString(R.string.configuration_target_list_empty_title),
                context.getString(
                    R.string.configuration_target_list_empty_content,
                    template.emptyListMessage
                ),
                null,
                onClick = listener::onListItemsEmptyMessageClicked
            )
        )
    }

    override fun getLabel(context: Context): String {
        return context.getString(R.string.configuration_target_list_items_title)
    }

    override fun createBlank(context: Context): TargetTemplate.ListItems {
        return TargetTemplate.ListItems(
            defaultTitle(context),
            defaultSubtitle(context),
            defaultIcon(),
            defaultClickAction(context),
            defaultTargetExtras(),
            listOf(defaultListItem(context, 1)),
            defaultIcon(),
            context.getString(R.string.configuration_target_list_empty_content_default)
        )
    }

    private fun List<Text>.describe(context: Context): String {
        return context.resources.getQuantityString(
            R.plurals.configuration_target_list_items_content, size, size
        )
    }

    interface ListItemsOptionsListener: TargetOptionsListener {
        fun onListItemsItemsClicked()
        fun onListItemsIconClicked()
        fun onListItemsEmptyMessageClicked()
    }

}