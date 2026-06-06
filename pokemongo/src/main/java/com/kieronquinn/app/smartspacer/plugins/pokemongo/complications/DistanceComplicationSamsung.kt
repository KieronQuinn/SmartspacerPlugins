package com.kieronquinn.app.smartspacer.plugins.pokemongo.complications

import com.kieronquinn.app.smartspacer.plugins.pokemongo.BuildConfig
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin
import com.kieronquinn.app.smartspacer.plugins.pokemongo.R

class DistanceComplicationSamsung: DistanceComplication() {

    override val variant = PokemonGoPlugin.Variant.SAMSUNG
    override val widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.distance.samsung"
    override val titleRes = R.string.complication_distance_label_samsung
    override val incompatibleRes = R.string.complication_incompatible_samsung
    
}