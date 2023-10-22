package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.visibility.target

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetVisibilityTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Target
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

abstract class TargetVisibilityViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(input: SmartspacerTargetVisibilityTaskerInput)
    abstract fun onSmartspacerTargetClicked()
    abstract fun setSmartspacerId(smartspacerId: String)
    abstract fun setVisibility(visible: Boolean)

    sealed class State {
        object Loading: State()
        data class Loaded(val target: Target?, val visible: Boolean): State()
    }

}

class TargetVisibilityViewModelImpl(
    private val navigation: ContainerNavigation,
    databaseRepository: DatabaseRepository
): TargetVisibilityViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)
    private val visible = MutableStateFlow<Boolean?>(null)

    private val target = smartspacerId.flatMapLatest {
        databaseRepository.getTargetAsFlow(
            it ?: return@flatMapLatest flowOf(null)
        )
    }

    override val state = combine(
        target,
        visible.filterNotNull()
    ) { t, v ->
        State.Loaded(t, v)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(input: SmartspacerTargetVisibilityTaskerInput) {
        if(smartspacerId.value != null || visible.value != null) return
        viewModelScope.launch {
            smartspacerId.emit(input.smartspacerId)
            visible.emit(input.visibility ?: true)
        }
    }

    override fun onSmartspacerTargetClicked() {
        viewModelScope.launch {
            navigation.navigate(TargetVisibilityFragmentDirections.actionTargetVisibilityFragmentToTargetPickerFragment2())
        }
    }

    override fun setSmartspacerId(smartspacerId: String) {
        viewModelScope.launch {
            this@TargetVisibilityViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun setVisibility(visible: Boolean) {
        viewModelScope.launch {
            this@TargetVisibilityViewModelImpl.visible.emit(visible)
        }
    }

}