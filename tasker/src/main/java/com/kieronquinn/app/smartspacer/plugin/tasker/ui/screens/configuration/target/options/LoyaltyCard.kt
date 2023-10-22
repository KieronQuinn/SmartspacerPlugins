package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options

import android.content.Context
import android.widget.ImageView.ScaleType
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.LoyaltyCard.LoyaltyCardOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultImage
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultSubtitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTargetExtras
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultTitle
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.TargetOptionsListener
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.label

class LoyaltyCard: TargetOptionsProvider<TargetTemplate.LoyaltyCard, LoyaltyCardOptionsListener> {

    override fun getOptions(
        context: Context,
        template: TargetTemplate.LoyaltyCard,
        listener: LoyaltyCardOptionsListener
    ): List<BaseSettingsItem> {
        return listOf(
            Setting(
                context.getString(R.string.configuration_loyalty_card_icon_title),
                template.cardIcon.describe(context),
                null,
                onClick = listener::onLoyaltyCardIconClicked
            ),
            Setting(
                context.getString(R.string.configuration_loyalty_card_prompt_title),
                template.cardPrompt.describe(),
                null,
                onClick = listener::onLoyaltyCardPromptClicked
            ),
            Dropdown(
                context.getString(R.string.configuration_loyalty_card_scale_type_title),
                context.getString(template.imageScaleType.label()),
                null,
                template.imageScaleType,
                listener::onLoyaltyCardScaleTypeChanged,
                ScaleType.values().toList()
            ) {
                it.label()
            },
            Setting(
                context.getString(R.string.configuration_loyalty_card_width_title),
                template._imageWidth
                    ?: context.getString(R.string.configuration_loyalty_card_width_content),
                null,
                onClick = listener::onLoyaltyCardWidthClicked
            ),
            Setting(
                context.getString(R.string.configuration_loyalty_card_height_title),
                template._imageHeight
                    ?: context.getString(R.string.configuration_loyalty_card_height_content),
                null,
                onClick = listener::onLoyaltyCardHeightClicked
            )
        )
    }

    override fun getLabel(context: Context): String {
        return context.getString(R.string.configuration_loyalty_card_title)
    }

    override fun createBlank(context: Context): TargetTemplate.LoyaltyCard {
        return TargetTemplate.LoyaltyCard(
            defaultTitle(context),
            defaultSubtitle(context),
            defaultIcon(),
            defaultClickAction(context),
            defaultTargetExtras(),
            defaultImage(context),
            Text(context.getString(R.string.configuration_loyalty_card_prompt_title))
        )
    }

    interface LoyaltyCardOptionsListener: TargetOptionsListener {
        fun onLoyaltyCardIconClicked()
        fun onLoyaltyCardPromptClicked()
        fun onLoyaltyCardScaleTypeChanged(scaleType: ScaleType)
        fun onLoyaltyCardWidthClicked()
        fun onLoyaltyCardHeightClicked()
    }

}