package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.tapaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTapActionEventInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.InputValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TapActionEventViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(input: SmartspacerTapActionEventInput)
    abstract fun onIdClicked()
    abstract fun onIdChanged(id: String)

    sealed class State {
        object Loading: State()
        data class Loaded(val id: String?): State()
    }

}

class TapActionEventViewModelImpl(
    private val navigation: ContainerNavigation
): TapActionEventViewModel() {

    private val id = MutableStateFlow<IdWrapper?>(null)

    override val state = id.filterNotNull().mapLatest {
        State.Loaded(it.id)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(input: SmartspacerTapActionEventInput) {
        viewModelScope.launch {
            if(id.value != null) return@launch
            id.emit(IdWrapper(input.id))
        }
    }

    override fun onIdClicked() {
        val current = state.value as? State.Loaded ?: return
        viewModelScope.launch {
            navigation.navigate(TapActionEventFragmentDirections.actionTapActionEventFragmentToNavGraphIncludeString(
                StringInputFragment.Config(
                    current.id ?: "",
                    TapActionEventFragment.REQUEST_KEY_ID,
                    R.string.tap_action_event_id_title,
                    R.string.tap_action_event_id_description,
                    R.string.tap_action_event_id_title,
                    inputValidation = InputValidation.TAP_ACTION_ID
                )
            ))
        }
    }

    override fun onIdChanged(id: String) {
        viewModelScope.launch {
            this@TapActionEventViewModelImpl.id.emit(IdWrapper(id))
        }
    }

    data class IdWrapper(val id: String?)

}