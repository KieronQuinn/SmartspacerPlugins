package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.requirement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerRequirementSetTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Requirement
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.DatabaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class RequirementUpdateViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(input: SmartspacerRequirementSetTaskerInput)
    abstract fun onSmartspacerRequirementClicked()
    abstract fun setSmartspacerId(smartspacerId: String?)
    abstract fun setIsMet(visible: Boolean)

    sealed class State {
        object Loading: State()
        data class Loaded(val requirement: Requirement?, val isMet: Boolean): State()
    }

}

class RequirementUpdateViewModelImpl(
    private val navigation: ContainerNavigation,
    databaseRepository: DatabaseRepository
): RequirementUpdateViewModel() {

    private val passedSmartspacerId = MutableStateFlow<String?>(null)
    private val setSmartspacerId = MutableStateFlow<String?>(null)

    private val smartspacerId = combine(
        passedSmartspacerId,
        setSmartspacerId
    ) { passed, set ->
        set ?: passed
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val passedIsMet = MutableStateFlow<Boolean?>(null)
    private val setIsMet = MutableStateFlow<Boolean?>(null)

    private val isMet = combine(
        passedIsMet,
        setIsMet
    ) { passed, set ->
        set ?: passed
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val requirement = smartspacerId.flatMapLatest {
        databaseRepository.getRequirementAsFlow(
            it ?: return@flatMapLatest flowOf(null)
        )
    }

    override val state = combine(
        requirement,
        isMet.filterNotNull()
    ) { t, v ->
        State.Loaded(t, v)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(input: SmartspacerRequirementSetTaskerInput) {
        viewModelScope.launch {
            passedIsMet.emit(input.isMet ?: true)
            passedSmartspacerId.emit(input.smartspacerId)
        }
    }

    override fun onSmartspacerRequirementClicked() {
        viewModelScope.launch {
            navigation.navigate(RequirementUpdateFragmentDirections.actionRequirementUpdateFragmentToRequirementPickerFragment())
        }
    }

    override fun setSmartspacerId(smartspacerId: String?) {
        viewModelScope.launch {
            setSmartspacerId.emit(smartspacerId)
        }
    }

    override fun setIsMet(isMet: Boolean) {
        viewModelScope.launch {
            setIsMet.emit(isMet)
        }
    }

}