package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.ComplicationTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.Basic.BasicOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider.Companion.defaultComplicationExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider.Companion.defaultContent
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider.ComplicationOptionsListener

class Basic: ComplicationOptionsProvider<ComplicationTemplate, BasicOptionsListener> {

    override fun createBlank(context: Context): ComplicationTemplate {
        return ComplicationTemplate.Basic(
            defaultIcon(),
            defaultContent(context),
            defaultClickAction(context),
            defaultComplicationExtras()
        )
    }

    override fun getOptions(
        context: Context,
        template: ComplicationTemplate,
        listener: BasicOptionsListener
    ): List<BaseSettingsItem> {
        //No Basic-specific options
        return emptyList()
    }

    override fun getLabel(context: Context): String {
        return context.getString(R.string.configuration_complication_basic_title)
    }

    interface BasicOptionsListener: ComplicationOptionsListener

}