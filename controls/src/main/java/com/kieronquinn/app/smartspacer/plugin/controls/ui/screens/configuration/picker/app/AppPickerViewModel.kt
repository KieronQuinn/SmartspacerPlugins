package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlsApp
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class AppPickerViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun onAppClicked(app: ControlsApp)

    sealed class State {
        data object Loading: State()
        data class Loaded(val apps: List<ControlsApp>): State()
    }

}

class AppPickerViewModelImpl(
    private val controlsRepository: ControlsRepository,
    private val navigation: ContainerNavigation
): AppPickerViewModel() {

    private val apps = flow {
        emit(controlsRepository.getControlsApps())
    }.flowOn(Dispatchers.IO)

    override val state = apps.mapLatest {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onAppClicked(app: ControlsApp) {
        viewModelScope.launch {
            navigation.navigate(AppPickerFragmentDirections
                .actionAppPickerFragmentToControlPickerFragment(app))
        }
    }

}