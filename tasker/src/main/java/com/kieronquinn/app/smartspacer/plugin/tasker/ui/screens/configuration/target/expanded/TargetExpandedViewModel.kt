package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.appshortcuts.TargetExpandedAppShortcutsFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.TargetExpandedShortcutsFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.TargetExpandedWidgetFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetExpandedViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: ExpandedState)
    abstract fun dismiss()
    abstract fun onWidgetClicked()
    abstract fun onWidgetChanged(widget: ExpandedState.Widget?)
    abstract fun onShortcutsClicked()
    abstract fun onShortcutsChanged(shortcuts: ExpandedState.Shortcuts)
    abstract fun onAppShortcutsClicked()
    abstract fun onAppShortcutsChanged(appShortcuts: ExpandedState.AppShortcuts?)

    sealed class State {
        object Loading: State()
        data class Loaded(val state: ExpandedState): State()
    }

}

class TargetExpandedViewModelImpl(
    private val navigation: ContainerNavigation
): TargetExpandedViewModel() {

    private val expandedState = MutableStateFlow<ExpandedState?>(null)

    override val state = expandedState.filterNotNull().map {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: ExpandedState) {
        if(expandedState.value != null) return
        viewModelScope.launch {
            expandedState.emit(current)
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onWidgetClicked() {
        val current = expandedState.value ?: return
        viewModelScope.launch {
            navigation.navigate(TargetExpandedFragmentDirections.actionTargetExpandedFragmentToTargetExpandedWidgetFragment(
                TargetExpandedWidgetFragment.Config(
                    TargetExpandedFragment.REQUEST_KEY_WIDGET,
                    current.widget
                )
            ))
        }
    }

    override fun onWidgetChanged(widget: ExpandedState.Widget?) {
        updateExpandedState {
            copy(widget = widget)
        }
    }

    override fun onShortcutsClicked() {
        val current = expandedState.value ?: return
        viewModelScope.launch {
            navigation.navigate(TargetExpandedFragmentDirections.actionTargetExpandedFragmentToTargetExpandedShortcutsFragment(
                TargetExpandedShortcutsFragment.Config(
                    TargetExpandedFragment.REQUEST_KEY_SHORTCUTS,
                    current.shortcuts ?: ExpandedState.Shortcuts(emptyList())
                )
            ))
        }
    }

    override fun onShortcutsChanged(shortcuts: ExpandedState.Shortcuts) {
        updateExpandedState {
            copy(shortcuts = shortcuts)
        }
    }

    override fun onAppShortcutsClicked() {
        val current = expandedState.value ?: return
        viewModelScope.launch {
            navigation.navigate(TargetExpandedFragmentDirections.actionTargetExpandedFragmentToTargetExpandedAppShortcutsFragment(
                TargetExpandedAppShortcutsFragment.Config(
                    TargetExpandedFragment.REQUEST_KEY_APP_SHORTCUTS,
                    current.appShortcuts
                )
            ))
        }
    }

    override fun onAppShortcutsChanged(appShortcuts: ExpandedState.AppShortcuts?) {
        updateExpandedState {
            copy(appShortcuts = appShortcuts)
        }
    }

    private fun updateExpandedState(block: ExpandedState.() -> ExpandedState) {
        viewModelScope.launch {
            expandedState.emit(block(expandedState.value ?: return@launch))
        }
    }

}