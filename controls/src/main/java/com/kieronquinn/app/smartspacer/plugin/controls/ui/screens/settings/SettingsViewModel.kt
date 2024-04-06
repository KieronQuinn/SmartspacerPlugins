package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsSettingsRepository.RefreshPeriod
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SettingsViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun onRefreshPeriodChanged(period: RefreshPeriod)
    abstract fun onRefreshOnScreenStateChanged(enabled: Boolean)

    sealed class State {
        data object Loading: State()
        data class Loaded(
            val refreshPeriod: RefreshPeriod,
            val refreshOnScreenStateChanged: Boolean
        ): State()
    }

}

class SettingsViewModelImpl(
    settingsRepository: ControlsSettingsRepository
): SettingsViewModel() {

    private val refreshPeriod = settingsRepository.refreshPeriod
    private val refreshOnScreenStateChanged = settingsRepository.refreshOnScreenStateChanged

    override val state = combine(
        refreshPeriod.asFlow(),
        refreshOnScreenStateChanged.asFlow()
    ) { refresh, state ->
        State.Loaded(refresh, state)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onRefreshPeriodChanged(period: RefreshPeriod) {
        viewModelScope.launch {
            refreshPeriod.set(period)
        }
    }

    override fun onRefreshOnScreenStateChanged(enabled: Boolean) {
        viewModelScope.launch {
            refreshOnScreenStateChanged.set(enabled)
        }
    }

}