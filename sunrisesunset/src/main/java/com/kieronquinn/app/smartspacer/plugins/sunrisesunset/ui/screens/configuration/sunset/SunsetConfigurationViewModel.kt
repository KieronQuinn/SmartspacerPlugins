package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.sunset

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.AlarmRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository.SunriseSunsetState
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.ConfigurationViewModelImpl

abstract class SunsetConfigurationViewModel(
    alarmRepository: AlarmRepository,
    navigation: ContainerNavigation,
    context: Context
): ConfigurationViewModelImpl(alarmRepository, navigation, context)

class SunsetConfigurationViewModelImpl(
    private val sunriseSunsetRepository: SunriseSunsetRepository,
    alarmRepository: AlarmRepository,
    navigation: ContainerNavigation,
    context: Context
): SunsetConfigurationViewModel(alarmRepository, navigation, context) {

    override fun getSunriseSunsetState(): SunriseSunsetState {
        return sunriseSunsetRepository.getSunsetState() ?: SunriseSunsetState()
    }

    override fun setSunriseSunsetState(state: SunriseSunsetState) {
        sunriseSunsetRepository.setSunsetState(state)
    }

    override fun onRefreshClicked() {
        sunriseSunsetRepository.calculateSunset()
    }

}