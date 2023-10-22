package com.kieronquinn.app.smartspacer.plugins.pokemongo.complications

import com.kieronquinn.app.smartspacer.plugins.pokemongo.BuildConfig
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin
import com.kieronquinn.app.smartspacer.plugins.pokemongo.R

class BuddyComplicationSamsung: BuddyComplication() {

    override val variant = PokemonGoPlugin.Variant.SAMSUNG
    override val widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.buddy.samsung"
    override val titleRes = R.string.complication_buddy_label_samsung
    override val incompatibleRes = R.string.complication_incompatible_samsung
    
}