package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.complications

import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.R
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.activities.ConfigurationActivity.NavGraphMapping

class SunsetComplication: BaseComplication() {

    override val type = "sunset"
    override val label = R.string.complication_sunset_label
    override val description = R.string.complication_sunset_description
    override val icon = R.drawable.ic_sunset

    override val settingsIntent by lazy {
        createIntent(provideContext(), NavGraphMapping.COMPLICATION_SUNSET)
    }

    override fun SunriseSunsetRepository.getState() = getSunsetState()
    override fun SunriseSunsetRepository.loadState() = calculateSunset()

}