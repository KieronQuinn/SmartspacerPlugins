package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.BuildConfig
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.AlarmRepository
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository.ShowFor
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository.SunriseSunsetState
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.ConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.utils.extensions.hasPermissions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface ConfigurationViewModel {

    val state: StateFlow<State>

    fun onResume()
    fun onAlarmPermissionClicked()
    fun onRefreshClicked()
    fun onShowBeforeChanged(showFor: ShowFor)
    fun onShowAfterChanged(showFor: ShowFor)

    sealed class State {
        data object Loading: State()
        data class Loaded(
            val hasLocationPermission: Boolean,
            val hasBackgroundLocationPermission: Boolean,
            val hasAlarmPermission: Boolean,
            val state: SunriseSunsetState
        ): State()
    }

}

abstract class ConfigurationViewModelImpl(
    private val alarmRepository: AlarmRepository,
    private val navigation: ContainerNavigation,
    context: Context
): ViewModel(), ConfigurationViewModel {

    private val sunriseSunsetChangedBus = MutableStateFlow(System.currentTimeMillis())
    private val resumeBus = MutableStateFlow(System.currentTimeMillis())

    private val sunriseSunsetState = sunriseSunsetChangedBus.mapLatest {
        getSunriseSunsetState()
    }

    private val hasLocationPermission = resumeBus.mapLatest {
        context.hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val hasBackgroundLocationPermission = resumeBus.mapLatest {
        context.hasPermissions(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private val hasAlarmPermission = resumeBus.mapLatest {
        alarmRepository.hasAlarmPermission()
    }

    override val state by lazy {
        combine(
            hasLocationPermission,
            hasBackgroundLocationPermission,
            hasAlarmPermission,
            sunriseSunsetState
        ) { location, background, alarm, state ->
            State.Loaded(location, background, alarm, state)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)
    }

    override fun onResume() {
        viewModelScope.launch {
            resumeBus.emit(System.currentTimeMillis())
        }
    }

    override fun onAlarmPermissionClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            })
        }
    }

    override fun onShowBeforeChanged(showFor: ShowFor) {
        updateSunriseSunsetState {
            copy(showBefore = showFor)
        }
    }

    override fun onShowAfterChanged(showFor: ShowFor) {
        updateSunriseSunsetState {
            copy(showAfter = showFor)
        }
    }

    private fun updateSunriseSunsetState(block: SunriseSunsetState.() -> SunriseSunsetState) {
        val current = (state.value as? State.Loaded)?.state ?: return
        setSunriseSunsetState(block(current))
        viewModelScope.launch {
            sunriseSunsetChangedBus.emit(System.currentTimeMillis())
        }
    }

    abstract fun getSunriseSunsetState(): SunriseSunsetState
    abstract fun setSunriseSunsetState(state: SunriseSunsetState)

}