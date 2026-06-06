package com.kieronquinn.app.smartspacer.plugins.pokemongo.complications

import com.kieronquinn.app.smartspacer.plugins.pokemongo.BuildConfig
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin
import com.kieronquinn.app.smartspacer.plugins.pokemongo.R

class DistanceComplicationPlay: DistanceComplication() {

    override val variant = PokemonGoPlugin.Variant.PLAY
    override val widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.distance.play"
    override val titleRes = R.string.complication_distance_label
    override val incompatibleRes = R.string.complication_incompatible
    
}