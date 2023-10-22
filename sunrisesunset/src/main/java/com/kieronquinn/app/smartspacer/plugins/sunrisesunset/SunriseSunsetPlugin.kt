package com.kieronquinn.app.smartspacer.plugins.sunrisesunset

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.AlarmRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.AlarmRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.sunrise.SunriseConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.sunrise.SunriseConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.sunset.SunsetConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.sunset.SunsetConfigurationViewModelImpl
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class SunriseSunsetPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single<AlarmRepository> { AlarmRepositoryImpl(get()) }
        single<SunriseSunsetRepository> { SunriseSunsetRepositoryImpl(get(), get(), get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<SunriseConfigurationViewModel> { SunriseConfigurationViewModelImpl(get(), get(), get(), get()) }
        viewModel<SunsetConfigurationViewModel> { SunsetConfigurationViewModelImpl(get(), get(), get(), get()) }
    }

    override fun onCreate() {
        super.onCreate()
        val alarmRepository by inject<AlarmRepository>()
        alarmRepository.scheduleNextSunriseAlarm()
        alarmRepository.scheduleNextSunsetAlarm()
    }

}