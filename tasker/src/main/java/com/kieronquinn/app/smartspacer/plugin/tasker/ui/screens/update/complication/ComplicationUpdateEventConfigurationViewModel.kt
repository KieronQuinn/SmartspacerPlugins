package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.update.complication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerComplicationUpdateInput
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Complication
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

abstract class ComplicationUpdateEventConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: SmartspacerComplicationUpdateInput?)
    abstract fun onComplicationClicked()
    abstract fun onSmartspacerIdChanged(id: String)

    sealed class State {
        object Loading: State()
        data class Loaded(val complication: Complication?): State()
    }

}

class ComplicationUpdateEventConfigurationViewModelImpl(
    private val navigation: ContainerNavigation,
    databaseRepository: DatabaseRepository
): ComplicationUpdateEventConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<SmartspacerIdWrapper?>(null)

    private val complication = smartspacerId.filterNotNull().flatMapLatest {
        if(it.id == null) return@flatMapLatest flowOf(null)
        databaseRepository.getComplicationAsFlow(it.id)
    }

    override val state = complication.mapLatest {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: SmartspacerComplicationUpdateInput?) {
        if(smartspacerId.value != null) return
        viewModelScope.launch {
            smartspacerId.emit(SmartspacerIdWrapper(current?.smartspacerId))
        }
    }

    override fun onComplicationClicked() {
        viewModelScope.launch {
            navigation.navigate(ComplicationUpdateEventConfigurationFragmentDirections
                .actionComplicationUpdateEventConfigurationFragmentToComplicationPickerFragment3())
        }
    }

    override fun onSmartspacerIdChanged(id: String) {
        viewModelScope.launch {
            smartspacerId.emit(SmartspacerIdWrapper(id))
        }
    }

    data class SmartspacerIdWrapper(val id: String?)

}