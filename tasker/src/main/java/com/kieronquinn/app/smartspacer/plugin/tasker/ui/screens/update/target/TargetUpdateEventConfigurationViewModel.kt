package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.update.target

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetUpdateInput
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

abstract class TargetUpdateEventConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: SmartspacerTargetUpdateInput?)
    abstract fun onTargetClicked()
    abstract fun onSmartspacerIdChanged(id: String)

    sealed class State {
        object Loading: State()
        data class Loaded(val target: Target?): State()
    }

}

class TargetUpdateEventConfigurationViewModelImpl(
    private val navigation: ContainerNavigation,
    databaseRepository: DatabaseRepository
): TargetUpdateEventConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<SmartspacerIdWrapper?>(null)

    private val target = smartspacerId.filterNotNull().flatMapLatest {
        if(it.id == null) return@flatMapLatest flowOf(null)
        databaseRepository.getTargetAsFlow(it.id)
    }

    override val state = target.mapLatest {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: SmartspacerTargetUpdateInput?) {
        if(smartspacerId.value != null) return
        viewModelScope.launch {
            smartspacerId.emit(SmartspacerIdWrapper(current?.smartspacerId))
        }
    }

    override fun onTargetClicked() {
        viewModelScope.launch {
            navigation.navigate(TargetUpdateEventConfigurationFragmentDirections
                .actionTargetUpdateEventConfigurationFragmentToTargetPickerFragment4())
        }
    }

    override fun onSmartspacerIdChanged(id: String) {
        viewModelScope.launch {
            smartspacerId.emit(SmartspacerIdWrapper(id))
        }
    }

    data class SmartspacerIdWrapper(val id: String?)

}