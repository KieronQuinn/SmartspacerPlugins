package com.kieronquinn.app.smartspacer.plugins.pokemongo.complications

import com.kieronquinn.app.smartspacer.plugins.pokemongo.R
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetConfiguration
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetType

abstract class BuddyComplication: PokemonGoComplication() {

    override val descriptionRes = R.string.complication_buddy_description
    override val iconRes = R.drawable.ic_complication_buddy
    override val widgetType = WidgetType.BUDDY

    override fun getWidgets(): List<WidgetConfiguration> {
        return listOfNotNull(widgetRepository.getBuddyConfiguration(variant))
    }

}