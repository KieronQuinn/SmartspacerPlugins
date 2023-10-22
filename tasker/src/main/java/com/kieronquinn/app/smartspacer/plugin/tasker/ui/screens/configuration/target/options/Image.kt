package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Image.ImageOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultImage
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultSubtitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTargetExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.TargetOptionsListener
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget

class Image: TargetOptionsProvider<TargetTemplate.Image, ImageOptionsListener> {

    override fun getOptions(
        context: Context,
        template: TargetTemplate.Image,
        listener: ImageOptionsListener
    ): List<BaseSettingsItem> {
        return listOf(
            Setting(
                context.getString(R.string.configuration_image_image_title),
                template.image.describe(context),
                null,
                onClick = listener::onImageImageClicked
            )
        )
    }

    override fun getLabel(context: Context): String {
        return context.getString(R.string.configuration_image_title)
    }

    override fun createBlank(context: Context): TargetTemplate.Image {
        return TargetTemplate.Image(
            defaultTitle(context),
            defaultSubtitle(context),
            defaultIcon(),
            defaultClickAction(context),
            defaultTargetExtras(),
            SmartspaceTarget.FEATURE_COMMUTE_TIME.toString(),
            defaultImage(context)
        )
    }

    interface ImageOptionsListener: TargetOptionsListener {
        fun onImageImageClicked()
    }

}