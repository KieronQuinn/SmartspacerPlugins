package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.shortcut

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState.Shortcuts.Shortcut
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetExpandedShortcutViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: Shortcut)
    abstract fun dismiss()
    abstract fun onTitleClicked()
    abstract fun onTitleChanged(title: Text)
    abstract fun onIconClicked()
    abstract fun onIconChanged(icon: Icon)
    abstract fun onTapActionClicked()
    abstract fun onTapActionChanged(tapAction: TapAction)
    abstract fun onShowWhenLockedChanged(enabled: Boolean)

    sealed class State {
        object Loading: State()
        data class Loaded(val shortcut: Shortcut): State()
    }

}

class TargetExpandedShortcutViewModelImpl(
    private val navigation: ContainerNavigation
): TargetExpandedShortcutViewModel() {

    private val shortcut = MutableStateFlow<Shortcut?>(null)

    override val state = shortcut.filterNotNull().mapLatest {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: Shortcut) {
        if(shortcut.value != null) return
        viewModelScope.launch {
            shortcut.emit(current)
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onTitleClicked() {
        val current = shortcut.value?.label ?: return
        viewModelScope.launch {
            navigation.navigate(TargetExpandedShortcutFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_target_expanded_shortcuts_shortcut_title_title,
                    TargetExpandedShortcutFragment.REQUEST_KEY_TITLE,
                    current
                )
            ))
        }
    }

    override fun onTitleChanged(title: Text) {
        updateShortcut {
            copy(label = title)
        }
    }

    override fun onIconClicked() {
        val current = shortcut.value?.icon ?: return
        viewModelScope.launch {
            navigation.navigate(TargetExpandedShortcutFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_target_expanded_shortcuts_shortcut_icon_title,
                    TargetExpandedShortcutFragment.REQUEST_KEY_ICON,
                    current
                )
            ))
        }
    }

    override fun onIconChanged(icon: Icon) {
        updateShortcut {
            copy(icon = icon)
        }
    }

    override fun onTapActionClicked() {
        val current = shortcut.value?.tapAction ?: return
        viewModelScope.launch {
            navigation.navigate(TargetExpandedShortcutFragmentDirections.actionGlobalNavGraphIncludeTapAction(
                TapActionFragment.Config(
                    R.string.configuration_target_expanded_shortcuts_shortcut_tap_action_title,
                    TargetExpandedShortcutFragment.REQUEST_KEY_TAP_ACTION,
                    current
                )
            ))
        }
    }

    override fun onTapActionChanged(tapAction: TapAction) {
        updateShortcut {
            copy(tapAction = tapAction)
        }
    }

    override fun onShowWhenLockedChanged(enabled: Boolean) {
        updateShortcut {
            copy(showWhenLocked = enabled)
        }
    }

    private fun updateShortcut(block: Shortcut.() -> Shortcut) {
        viewModelScope.launch {
            shortcut.emit(block(shortcut.value ?: return@launch))
        }
    }

}