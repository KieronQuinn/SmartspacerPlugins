package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options

import android.content.Context
import android.widget.ImageView.ScaleType
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Doorbell.DoorbellState
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Doorbell.DoorbellState.DoorbellStateType
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Doorbell.DoorbellOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultImage
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultSubtitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTargetExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.TargetOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.label
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget

class Doorbell: TargetOptionsProvider<TargetTemplate.Doorbell, DoorbellOptionsListener> {

    override fun getOptions(
        context: Context,
        template: TargetTemplate.Doorbell,
        listener: DoorbellOptionsListener
    ): List<BaseSettingsItem> {
        val provider = when(template.doorbellState) {
            is DoorbellState.LoadingIndeterminate -> LoadingIndeterminate()
            is DoorbellState.Loading -> Loading()
            is DoorbellState.Videocam -> Videocam()
            is DoorbellState.VideocamOff -> VideocamOff()
            is DoorbellState.ImageBitmap -> ImageBitmap()
            is DoorbellState.ImageUri -> ImageUri()
        }
        return listOf(Setting(
            context.getString(R.string.configuration_doorbell_state_title),
            context.getString(
                R.string.configuration_doorbell_state_content, provider.getName(context)
            ),
            null,
            onClick = listener::onDoorbellStateClicked
        )) + provider.getOptionsWithCast(context, template.doorbellState, listener)
    }

    override fun getLabel(context: Context): String {
        return context.getString(R.string.configuration_doorbell_title)
    }

    override fun createBlank(context: Context): TargetTemplate.Doorbell {
        return TargetTemplate.Doorbell(
            defaultTitle(context),
            defaultSubtitle(context),
            defaultIcon(),
            defaultClickAction(context),
            defaultTargetExtras(),
            SmartspaceTarget.FEATURE_DOORBELL.toString(),
            DoorbellState.LoadingIndeterminate(null, null)
        )
    }

    interface DoorbellOptionsListener: TargetOptionsListener {
        fun onDoorbellStateClicked()
        fun onDoorbellWidthClicked()
        fun onDoorbellHeightClicked()
        fun onDoorbellRatioWidthClicked()
        fun onDoorbellRatioHeightClicked()
        fun onDoorbellIconClicked()
        fun onDoorbellShowLoadingBarChanged(enabled: Boolean)
        fun onDoorbellImageBitmapClicked()
        fun onDoorbellImageScaleTypeChanged(scaleType: ScaleType)
        fun onDoorbellImagesClicked()
        fun onDoorbellFrameDurationClicked()
    }

    interface DoorbellOptionsProvider<D: DoorbellState> {
        companion object {
            fun getProvider(type: DoorbellStateType): DoorbellOptionsProvider<*> {
                return when(type) {
                    DoorbellStateType.LOADING_INDETERMINATE -> LoadingIndeterminate()
                    DoorbellStateType.LOADING -> Loading()
                    DoorbellStateType.VIDEOCAM -> Videocam()
                    DoorbellStateType.VIDEOCAM_OFF -> VideocamOff()
                    DoorbellStateType.IMAGE_BITMAP -> ImageBitmap()
                    DoorbellStateType.IMAGE_URI -> ImageUri()
                }
            }
        }
        
        fun getName(context: Context): String
        fun getDescription(context: Context): String
        
        fun createBlank(context: Context): D

        @Suppress("UNCHECKED_CAST")
        fun getOptionsWithCast(
            context: Context,
            state: DoorbellState,
            listener: DoorbellOptionsListener
        ): List<BaseSettingsItem> {
            return getOptions(context, state as D, listener)
        }

        fun getOptions(
            context: Context,
            state: D,
            listener: DoorbellOptionsListener
        ): List<BaseSettingsItem>
    }

    private class LoadingIndeterminate: DoorbellOptionsProvider<DoorbellState.LoadingIndeterminate> {
        override fun getName(context: Context): String {
            return context.getString(R.string.configuration_doorbell_loading_indeterminate_title)
        }

        override fun getDescription(context: Context): String {
            return context.getString(R.string.configuration_doorbell_loading_indeterminate_content)
        }

        override fun createBlank(context: Context): DoorbellState.LoadingIndeterminate {
            return DoorbellState.LoadingIndeterminate(null, null)
        }

        override fun getOptions(
            context: Context,
            state: DoorbellState.LoadingIndeterminate,
            listener: DoorbellOptionsListener
        ): List<BaseSettingsItem> {
            return listOf(
                Setting(
                    context.getString(R.string.configuration_doorbell_width_title),
                    state._width
                        ?: context.getString(R.string.configuration_doorbell_width_content),
                    null,
                    onClick = listener::onDoorbellWidthClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_height_title),
                    state._height
                        ?: context.getString(R.string.configuration_doorbell_height_content),
                    null,
                    onClick = listener::onDoorbellHeightClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_ratio_width_title),
                    state._ratioWidth ?: state.ratioWidth.toString(),
                    null,
                    onClick = listener::onDoorbellRatioWidthClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_ratio_height_title),
                    state._ratioHeight ?: state.ratioHeight.toString(),
                    null,
                    onClick = listener::onDoorbellRatioHeightClicked
                )
            )
        }
    }

    private class Loading: DoorbellOptionsProvider<DoorbellState.Loading> {
        override fun getName(context: Context): String {
            return context.getString(R.string.configuration_doorbell_loading_title)
        }

        override fun getDescription(context: Context): String {
            return context.getString(R.string.configuration_doorbell_loading_content)
        }

        override fun createBlank(context: Context): DoorbellState.Loading {
            return DoorbellState.Loading(defaultIcon())
        }

        override fun getOptions(
            context: Context,
            state: DoorbellState.Loading,
            listener: DoorbellOptionsListener
        ): List<BaseSettingsItem> {
            return listOf(
                Setting(
                    context.getString(R.string.configuration_doorbell_loading_icon_title),
                    state.icon.describe(context),
                    null,
                    onClick = listener::onDoorbellIconClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_width_title),
                    state._width
                        ?: context.getString(R.string.configuration_doorbell_width_content),
                    null,
                    onClick = listener::onDoorbellWidthClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_height_title),
                    state._width
                        ?: context.getString(R.string.configuration_doorbell_height_content),
                    null,
                    onClick = listener::onDoorbellHeightClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_ratio_width_title),
                    state._ratioWidth ?: state.ratioWidth.toString(),
                    null,
                    onClick = listener::onDoorbellRatioWidthClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_ratio_height_title),
                    state._ratioHeight ?: state.ratioHeight.toString(),
                    null,
                    onClick = listener::onDoorbellRatioWidthClicked
                ),
                SwitchSetting(
                    state.showProgressBar,
                    context.getString(R.string.configuration_doorbell_loading_show_progress_bar_title),
                    context.getString(R.string.configuration_doorbell_loading_show_progress_bar_content),
                    null,
                    onChanged = listener::onDoorbellShowLoadingBarChanged
                )
            )
        }
    }

    private class Videocam: DoorbellOptionsProvider<DoorbellState.Videocam> {
        override fun getName(context: Context): String {
            return context.getString(R.string.configuration_doorbell_videocam_title)
        }

        override fun getDescription(context: Context): String {
            return context.getString(R.string.configuration_doorbell_videocam_content)
        }

        override fun createBlank(context: Context): DoorbellState.Videocam {
            return DoorbellState.Videocam(null, null)
        }

        override fun getOptions(
            context: Context,
            state: DoorbellState.Videocam,
            listener: DoorbellOptionsListener
        ): List<BaseSettingsItem> {
            return listOf(
                Setting(
                    context.getString(R.string.configuration_doorbell_width_title),
                    state._width
                        ?: context.getString(R.string.configuration_doorbell_width_content),
                    null,
                    onClick = listener::onDoorbellWidthClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_height_title),
                    state._width
                        ?: context.getString(R.string.configuration_doorbell_height_content),
                    null,
                    onClick = listener::onDoorbellHeightClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_ratio_width_title),
                    state._ratioWidth ?: state.ratioWidth.toString(),
                    null,
                    onClick = listener::onDoorbellRatioWidthClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_ratio_height_title),
                    state._ratioHeight ?: state.ratioHeight.toString(),
                    null,
                    onClick = listener::onDoorbellRatioWidthClicked
                )
            )
        }
    }

    private class VideocamOff: DoorbellOptionsProvider<DoorbellState.VideocamOff> {
        override fun getName(context: Context): String {
            return context.getString(R.string.configuration_doorbell_videocam_off_title)
        }

        override fun getDescription(context: Context): String {
            return context.getString(R.string.configuration_doorbell_videocam_off_content)
        }

        override fun createBlank(context: Context): DoorbellState.VideocamOff {
            return DoorbellState.VideocamOff(null, null)
        }

        override fun getOptions(
            context: Context,
            state: DoorbellState.VideocamOff,
            listener: DoorbellOptionsListener
        ): List<BaseSettingsItem> {
            return listOf(
                Setting(
                    context.getString(R.string.configuration_doorbell_width_title),
                    state._width
                        ?: context.getString(R.string.configuration_doorbell_width_content),
                    null,
                    onClick = listener::onDoorbellWidthClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_height_title),
                    state._width
                        ?: context.getString(R.string.configuration_doorbell_height_content),
                    null,
                    onClick = listener::onDoorbellHeightClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_ratio_width_title),
                    state._ratioWidth ?: state.ratioWidth.toString(),
                    null,
                    onClick = listener::onDoorbellRatioWidthClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_ratio_height_title),
                    state._ratioHeight ?: state.ratioHeight.toString(),
                    null,
                    onClick = listener::onDoorbellRatioWidthClicked
                )
            )
        }
    }

    private class ImageBitmap: DoorbellOptionsProvider<DoorbellState.ImageBitmap> {
        override fun getName(context: Context): String {
            return context.getString(R.string.configuration_doorbell_image_title)
        }
        
        override fun getDescription(context: Context): String {
            return context.getString(R.string.configuration_doorbell_image_content)
        }

        override fun createBlank(context: Context): DoorbellState.ImageBitmap {
            return DoorbellState.ImageBitmap(defaultImage(context))
        }

        override fun getOptions(
            context: Context,
            state: DoorbellState.ImageBitmap,
            listener: DoorbellOptionsListener
        ): List<BaseSettingsItem> {
            return listOf(
                Setting(
                    context.getString(R.string.configuration_doorbell_image_title),
                    state.bitmap.describe(context),
                    null,
                    onClick = listener::onDoorbellImageBitmapClicked
                ),
                Dropdown(
                    context.getString(R.string.configuration_doorbell_image_scale_type_title),
                    context.getString(state.imageScaleType.label()),
                    null,
                    state.imageScaleType,
                    listener::onDoorbellImageScaleTypeChanged,
                    ScaleType.values().toList()
                ) {
                    it.label()
                },
                Setting(
                    context.getString(R.string.configuration_doorbell_width_title),
                    state._imageWidth
                        ?: context.getString(R.string.configuration_doorbell_width_content),
                    null,
                    onClick = listener::onDoorbellWidthClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_height_title),
                    state._imageHeight
                        ?: context.getString(R.string.configuration_doorbell_height_content),
                    null,
                    onClick = listener::onDoorbellHeightClicked
                )
            )
        }
    }

    private class ImageUri: DoorbellOptionsProvider<DoorbellState.ImageUri> {
        override fun getName(context: Context): String {
            return context.getString(R.string.configuration_doorbell_images_title)
        }

        override fun getDescription(context: Context): String {
            return context.getString(R.string.configuration_doorbell_images_content)
        }

        override fun createBlank(context: Context): DoorbellState.ImageUri {
            return DoorbellState.ImageUri("1000", listOf(defaultImage(context)))
        }

        override fun getOptions(
            context: Context,
            state: DoorbellState.ImageUri,
            listener: DoorbellOptionsListener
        ): List<BaseSettingsItem> {
            return listOf(
                Setting(
                    context.getString(R.string.configuration_doorbell_images_title),
                    context.resources.getQuantityString(
                        R.plurals.configuration_doorbell_images_uris,
                        state.imageUris.size,
                        state.imageUris.size
                    ),
                    null,
                    onClick = listener::onDoorbellImagesClicked
                ),
                Setting(
                    context.getString(R.string.configuration_doorbell_images_frame_duration),
                    context.getString(
                        R.string.configuration_doorbell_images_frame_duration_content,
                        state._frameDurationMs
                    ),
                    null,
                    onClick = listener::onDoorbellFrameDurationClicked
                )
            )
        }
    }

}