package com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.HealthMetric
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.GoogleHealthRepository
import com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens.focus.HealthConfigurationFocusViewModel.State.Loaded.Item
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class HealthConfigurationFocusViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun dismiss()

    sealed class State {
        data object Loading: State()
        data class Loaded(val items: List<Item>): State() {
            data class Item(val metric: HealthMetric, val available: Boolean)
        }
    }

}

class HealthConfigurationFocusViewModelImpl(
    private val navigation: ContainerNavigation,
    googleHealthRepository: GoogleHealthRepository
): HealthConfigurationFocusViewModel() {

    private val metrics = HealthMetric.entries.map { metric ->
        googleHealthRepository.isHealthMetricAvailable(metric).map {
            metric to it
        }
    }.toTypedArray()

    override val state = combine(*metrics) {
        State.Loaded(it.map { i -> Item(i.first, i.second) })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

}