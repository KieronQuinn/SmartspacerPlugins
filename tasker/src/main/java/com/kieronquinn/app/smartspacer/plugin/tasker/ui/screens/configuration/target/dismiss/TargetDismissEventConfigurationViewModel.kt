package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.dismiss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetDismissInput
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Target
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetDismissEventConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: SmartspacerTargetDismissInput?)
    abstract fun onTargetClicked()
    abstract fun onSmartspacerIdChanged(id: String)

    sealed class State {
        object Loading: State()
        data class Loaded(val target: Target?): State()
    }

}

class TargetDismissEventConfigurationViewModelImpl(
    private val navigation: ContainerNavigation,
    databaseRepository: DatabaseRepository
): TargetDismissEventConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<SmartspacerIdWrapper?>(null)

    private val target = smartspacerId.filterNotNull().flatMapLatest {
        if(it.id == null) return@flatMapLatest flowOf(null)
        databaseRepository.getTargetAsFlow(it.id)
    }

    override val state = target.mapLatest {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: SmartspacerTargetDismissInput?) {
        if(smartspacerId.value != null) return
        viewModelScope.launch {
            smartspacerId.emit(SmartspacerIdWrapper(current?.smartspacerId))
        }
    }

    override fun onTargetClicked() {
        viewModelScope.launch {
            navigation.navigate(TargetDismissEventConfigurationFragmentDirections
                .actionTargetDismissEventConfigurationFragmentToTargetPickerFragment3())
        }
    }

    override fun onSmartspacerIdChanged(id: String) {
        viewModelScope.launch {
            smartspacerId.emit(SmartspacerIdWrapper(id))
        }
    }

    data class SmartspacerIdWrapper(val id: String?)

}