package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.requirement.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Requirement
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class RequirementPickerViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun dismiss()

    sealed class State {
        object Loading: State()
        data class Loaded(val requirements: List<Requirement>): State()
    }

}

class RequirementPickerViewModelImpl(
    private val navigation: ContainerNavigation,
    databaseRepository: DatabaseRepository
): RequirementPickerViewModel() {

    override val state = databaseRepository.getAllRequirements().mapLatest {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

}