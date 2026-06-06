package com.kieronquinn.app.smartspacer.plugins.pokemongo.complications

import com.kieronquinn.app.smartspacer.plugins.pokemongo.R
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetConfiguration
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetType

abstract class DistanceComplication: PokemonGoComplication() {

    override val descriptionRes = R.string.complication_distance_description
    override val iconRes = R.drawable.ic_complication_distance
    override val widgetType = WidgetType.DISTANCE
    override val hasConfiguration = false

    override fun getWidgets(): List<WidgetConfiguration> {
        return listOfNotNull(widgetRepository.getDistanceConfiguration(variant))
    }

}