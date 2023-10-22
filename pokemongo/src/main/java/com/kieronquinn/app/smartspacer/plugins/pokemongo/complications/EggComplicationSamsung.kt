package com.kieronquinn.app.smartspacer.plugins.pokemongo.complications

import com.kieronquinn.app.smartspacer.plugins.pokemongo.BuildConfig
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin.Variant
import com.kieronquinn.app.smartspacer.plugins.pokemongo.R

class EggComplicationSamsung: EggComplication() {

    override val variant = Variant.SAMSUNG
    override val widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.egg.samsung"
    override val titleRes = R.string.complication_egg_label_samsung
    override val incompatibleRes = R.string.complication_incompatible_samsung

}