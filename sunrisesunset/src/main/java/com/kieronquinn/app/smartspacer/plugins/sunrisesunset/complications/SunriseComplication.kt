package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.complications

import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.R
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.activities.ConfigurationActivity.NavGraphMapping

class SunriseComplication: BaseComplication() {

    override val type = "sunrise"
    override val label = R.string.complication_sunrise_label
    override val description = R.string.complication_sunrise_description
    override val icon = R.drawable.ic_sunrise

    override val settingsIntent by lazy {
        createIntent(provideContext(), NavGraphMapping.COMPLICATION_SUNRISE)
    }

    override fun SunriseSunsetRepository.getState() = getSunriseState()
    override fun SunriseSunsetRepository.loadState() = calculateSunrise()

}