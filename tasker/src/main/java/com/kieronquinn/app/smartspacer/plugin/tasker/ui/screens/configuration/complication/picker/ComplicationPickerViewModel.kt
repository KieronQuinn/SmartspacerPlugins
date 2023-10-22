package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Complication
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class ComplicationPickerViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun dismiss()

    sealed class State {
        object Loading: State()
        data class Loaded(val complications: List<Complication>): State()
    }

}

class ComplicationPickerViewModelImpl(
    private val navigation: ContainerNavigation,
    databaseRepository: DatabaseRepository
): ComplicationPickerViewModel() {

    override val state = databaseRepository.getAllComplications().mapLatest {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

}