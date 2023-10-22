package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.sunrise

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.AlarmRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository.SunriseSunsetState
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.ConfigurationViewModelImpl

abstract class SunriseConfigurationViewModel(
    alarmRepository: AlarmRepository,
    navigation: ContainerNavigation,
    context: Context
): ConfigurationViewModelImpl(alarmRepository, navigation, context)

class SunriseConfigurationViewModelImpl(
    private val sunriseSunsetRepository: SunriseSunsetRepository,
    alarmRepository: AlarmRepository,
    navigation: ContainerNavigation,
    context: Context
): SunriseConfigurationViewModel(alarmRepository, navigation, context) {

    override fun getSunriseSunsetState(): SunriseSunsetState {
        return sunriseSunsetRepository.getSunriseState() ?: SunriseSunsetState()
    }

    override fun setSunriseSunsetState(state: SunriseSunsetState) {
        sunriseSunsetRepository.setSunriseState(state)
    }

    override fun onRefreshClicked() {
        sunriseSunsetRepository.calculateSunrise()
    }

}