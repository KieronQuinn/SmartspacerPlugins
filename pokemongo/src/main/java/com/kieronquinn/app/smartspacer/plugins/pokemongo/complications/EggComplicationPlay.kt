package com.kieronquinn.app.smartspacer.plugins.pokemongo.complications

import com.kieronquinn.app.smartspacer.plugins.pokemongo.BuildConfig
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin.Variant
import com.kieronquinn.app.smartspacer.plugins.pokemongo.R

class EggComplicationPlay: EggComplication() {

    override val variant = Variant.PLAY
    override val widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.egg.play"
    override val titleRes = R.string.complication_egg_label
    override val incompatibleRes = R.string.complication_incompatible

}