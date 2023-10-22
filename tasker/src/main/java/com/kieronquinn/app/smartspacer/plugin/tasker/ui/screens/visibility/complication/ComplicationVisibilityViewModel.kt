package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.visibility.complication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerComplicationVisibilityTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Complication
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

abstract class ComplicationVisibilityViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(input: SmartspacerComplicationVisibilityTaskerInput)
    abstract fun onSmartspacerComplicationClicked()
    abstract fun setSmartspacerId(smartspacerId: String)
    abstract fun setVisibility(visible: Boolean)

    sealed class State {
        object Loading: State()
        data class Loaded(val complication: Complication?, val visible: Boolean): State()
    }

}

class ComplicationVisibilityViewModelImpl(
    private val navigation: ContainerNavigation,
    databaseRepository: DatabaseRepository
): ComplicationVisibilityViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)
    private val visible = MutableStateFlow<Boolean?>(null)

    private val complication = smartspacerId.flatMapLatest {
        databaseRepository.getComplicationAsFlow(
            it ?: return@flatMapLatest flowOf(null)
        )
    }

    override val state = combine(
        complication,
        visible.filterNotNull()
    ) { t, v ->
        State.Loaded(t, v)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(input: SmartspacerComplicationVisibilityTaskerInput) {
        if(smartspacerId.value != null || visible.value != null) return
        viewModelScope.launch {
            smartspacerId.emit(input.smartspacerId)
            visible.emit(input.visibility ?: true)
        }
    }

    override fun onSmartspacerComplicationClicked() {
        viewModelScope.launch {
            navigation.navigate(ComplicationVisibilityFragmentDirections.actionComplicationVisibilityFragmentToComplicationPickerFragment2())
        }
    }

    override fun setSmartspacerId(smartspacerId: String) {
        viewModelScope.launch {
            this@ComplicationVisibilityViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun setVisibility(visible: Boolean) {
        viewModelScope.launch {
            this@ComplicationVisibilityViewModelImpl.visible.emit(visible)
        }
    }

}