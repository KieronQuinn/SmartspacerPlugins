package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState.Shortcuts
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState.Shortcuts.Shortcut
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.shortcut.TargetExpandedShortcutFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultClickAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.TargetOptionsProvider.Companion.defaultIcon
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.modify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetExpandedShortcutsViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: Shortcuts?)
    abstract fun dismiss()
    abstract fun onAddClicked(context: Context)
    abstract fun onShortcutDeleted(index: Int)
    abstract fun onShortcutClicked(key: String, shortcut: Shortcut)
    abstract fun onShortcutChanged(index: Int, shortcut: Shortcut)

    sealed class State {
        object Loading: State()
        data class Loaded(val shortcuts: Shortcuts, val showFab: Boolean): State()
    }

}

class TargetExpandedShortcutsViewModelImpl(
    private val navigation: ContainerNavigation
): TargetExpandedShortcutsViewModel() {

    companion object {
        const val MAX_ITEMS = 10
    }

    private val shortcuts = MutableStateFlow<Shortcuts?>(null)

    override val state = shortcuts.filterNotNull().mapLatest {
        State.Loaded(it, it.shortcuts.size < MAX_ITEMS)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: Shortcuts?) {
        if(shortcuts.value != null) return
        viewModelScope.launch {
            shortcuts.emit(current)
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onAddClicked(context: Context) {
        val current = shortcuts.value ?: return
        val newShortcut = Shortcut(
            Text(context.getString(R.string.configuration_target_expanded_shortcuts_shortcut_title)),
            defaultIcon(),
            defaultClickAction(context)
        )
        viewModelScope.launch {
            shortcuts.emit(current.copy(shortcuts = current.shortcuts.plus(newShortcut)))
        }
    }

    override fun onShortcutDeleted(index: Int) {
        val current = shortcuts.value ?: return
        val shortcutToDelete = current.shortcuts.getOrNull(index) ?: return
        viewModelScope.launch {
            shortcuts.emit(current.copy(shortcuts = current.shortcuts.minus(shortcutToDelete)))
        }
    }

    override fun onShortcutClicked(key: String, shortcut: Shortcut) {
        viewModelScope.launch {
            navigation.navigate(TargetExpandedShortcutsFragmentDirections.actionTargetExpandedShortcutsFragmentToTargetExpandedShortcutFragment(
                TargetExpandedShortcutFragment.Config(key, shortcut)
            ))
        }
    }

    override fun onShortcutChanged(index: Int, shortcut: Shortcut) {
        val current = shortcuts.value ?: return
        viewModelScope.launch {
            shortcuts.emit(current.copy(shortcuts = current.shortcuts.modify(index) { shortcut }))
        }
    }

}