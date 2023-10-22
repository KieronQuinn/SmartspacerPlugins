package com.kieronquinn.app.smartspacer.plugins.pokemongo.complications

import com.kieronquinn.app.smartspacer.plugins.pokemongo.BuildConfig
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin
import com.kieronquinn.app.smartspacer.plugins.pokemongo.R

class BuddyComplicationPlay: BuddyComplication() {

    override val variant = PokemonGoPlugin.Variant.PLAY
    override val widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.buddy.play"
    override val titleRes = R.string.complication_buddy_label
    override val incompatibleRes = R.string.complication_incompatible
    
}