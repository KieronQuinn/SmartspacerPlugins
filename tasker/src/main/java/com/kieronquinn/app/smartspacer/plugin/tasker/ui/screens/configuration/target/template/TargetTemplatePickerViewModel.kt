package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.template

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetTemplatePickerViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: Class<out TargetTemplate>)
    abstract fun dismiss()

    sealed class State {
        object Loading: State()
        data class Loaded(val current: Class<out TargetTemplate>): State()
    }

}

class TargetTemplatePickerViewModelImpl(
    private val navigation: ContainerNavigation
): TargetTemplatePickerViewModel() {

    private val current = MutableStateFlow<Class<out TargetTemplate>?>(null)

    override val state = current.filterNotNull().map {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: Class<out TargetTemplate>) {
        viewModelScope.launch {
            this@TargetTemplatePickerViewModelImpl.current.emit(current)
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

}