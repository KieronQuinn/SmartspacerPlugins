package com.kieronquinn.app.smartspacer.plugins.pokemongo.complications

import com.kieronquinn.app.smartspacer.plugins.pokemongo.R
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetConfiguration
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetType

abstract class EggComplication: PokemonGoComplication() {

    override val descriptionRes = R.string.complication_egg_description
    override val iconRes = R.drawable.ic_complication_egg
    override val widgetType = WidgetType.EGG

    override fun getWidgets(): List<WidgetConfiguration> {
        return widgetRepository.getEggConfiguration(variant)?.eggs ?: emptyList()
    }

}