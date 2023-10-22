package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Carousel.CarouselItem
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.options.ComplicationOptionsProvider.ComplicationOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Carousel.CarouselOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultSubtitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTargetExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.TargetOptionsListener

class Carousel: TargetOptionsProvider<TargetTemplate.Carousel, CarouselOptionsListener> {

    companion object {
        fun defaultCarouselItem(context: Context): CarouselItem {
            return CarouselItem(
                Text(context.getString(R.string.configuration_carousel_item_default_top)),
                Text(context.getString(R.string.configuration_carousel_item_default_bottom)),
                defaultIcon(),
                defaultClickAction(context)
            )
        }
    }

    override fun getOptions(
        context: Context,
        template: TargetTemplate.Carousel,
        listener: CarouselOptionsListener
    ): List<BaseSettingsItem> {
        return listOf(
            Setting(
                context.getString(R.string.configuration_carousel_items),
                context.resources.getQuantityString(
                    R.plurals.configuration_carousel_items,
                    template.carouselItems.size,
                    template.carouselItems.size
                ),
                null,
                onClick = listener::onCarouselItemsClicked
            ),
            Setting(
                context.getString(R.string.configuration_carousel_click_title),
                context.getString(R.string.configuration_carousel_click_content),
                null,
                onClick = listener::onCarouselTapActionClicked
            ),
            Header(context.getString(R.string.configuration_carousel_sub_complication_header)),
            SwitchSetting(
                template.subComplication != null,
                context.getString(R.string.configuration_carousel_sub_complication_title),
                context.getString(R.string.configuration_carousel_sub_complication_content),
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
        return context.getString(R.string.configuration_carousel_title)
    }

    override fun createBlank(context: Context): TargetTemplate.Carousel {
        return TargetTemplate.Carousel(
            defaultTitle(context),
            defaultSubtitle(context),
            defaultIcon(),
            defaultClickAction(context),
            defaultTargetExtras(),
            listOf(defaultCarouselItem(context)),
            null,
            null
        )
    }

    interface CarouselOptionsListener: TargetOptionsListener, ComplicationOptionsListener {
        fun onTargetSubComplicationEnabledChanged(enabled: Boolean)
        fun onCarouselItemsClicked()
        fun onCarouselTapActionClicked()
    }

}