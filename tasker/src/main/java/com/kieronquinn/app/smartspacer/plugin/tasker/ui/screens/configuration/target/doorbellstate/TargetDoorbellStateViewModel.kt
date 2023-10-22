package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.doorbellstate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Doorbell.DoorbellState.DoorbellStateType
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Doorbell
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetDoorbellStateViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: DoorbellStateType)
    abstract fun dismiss()

    sealed class State {
        object Loading: State()
        data class Loaded(
            val doorbellStates: Map<DoorbellStateType, Doorbell.DoorbellOptionsProvider<*>>,
            val selected: DoorbellStateType
        ): State()
    }

}

class TargetDoorbellStateViewModelImpl(
    private val navigation: ContainerNavigation
): TargetDoorbellStateViewModel() {

    private val doorbellState = MutableStateFlow<DoorbellStateType?>(null)

    private val allStates = DoorbellStateType.values().associateWith {
        Doorbell.DoorbellOptionsProvider.getProvider(it)
    }

    override val state = doorbellState.filterNotNull().mapLatest {
        State.Loaded(allStates, it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: DoorbellStateType) {
        if(doorbellState.value != null) return
        viewModelScope.launch {
            doorbellState.emit(current)
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

}