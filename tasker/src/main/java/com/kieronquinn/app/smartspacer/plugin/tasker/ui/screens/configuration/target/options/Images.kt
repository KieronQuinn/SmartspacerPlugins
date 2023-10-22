package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultImage
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultSubtitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTargetExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.TargetOptionsListener

class Images: TargetOptionsProvider<TargetTemplate.Images, Images.ImagesOptionsListener> {

    override fun getOptions(
        context: Context,
        template: TargetTemplate.Images,
        listener: ImagesOptionsListener
    ): List<BaseSettingsItem> {
        return listOf(
            Setting(
                context.getString(R.string.configuration_doorbell_images_title),
                context.resources.getQuantityString(
                    R.plurals.configuration_images_images,
                    template.images.size,
                    template.images.size
                ),
                null,
                onClick = listener::onImagesImagesClicked
            ),
            Setting(
                context.getString(R.string.configuration_images_click_title),
                template.imageClickIntent?.describe(context) ?:
                    context.getString(R.string.configuration_images_click_content),
                null,
                onClick = listener::onImagesTapActionClicked
            ),
            Setting(
                context.getString(R.string.configuration_images_image_dimension_ratio_title),
                template.imageDimensionRatio
                    ?: context.getString(R.string.configuration_images_image_dimension_ratio_content),
                null,
                onClick = listener::onImagesDimensionRatioClicked
            ),
            Setting(
                context.getString(R.string.configuration_images_frame_duration),
                "${template._frameDurationMs ?: template.frameDurationMs.toString()} ${
                    context.getString(R.string.input_string_suffix_frame_duration)
                }",
                null,
                onClick = listener::onImagesFrameDurationClicked
            )
        )
    }

    override fun getLabel(context: Context): String {
        return context.getString(R.string.configuration_images_title)
    }

    override fun createBlank(context: Context): TargetTemplate.Images {
        return TargetTemplate.Images(
            defaultTitle(context),
            defaultSubtitle(context),
            defaultIcon(),
            defaultClickAction(context),
            defaultTargetExtras(),
            listOf(defaultImage(context))
        )
    }

    override fun getConfig(): TargetOptionsProvider.Config {
        return TargetOptionsProvider.Config(
            supportsBundleExtras = false
        )
    }

    interface ImagesOptionsListener: TargetOptionsListener {
        fun onImagesImagesClicked()
        fun onImagesTapActionClicked()
        fun onImagesDimensionRatioClicked()
        fun onImagesFrameDurationClicked()
    }

}