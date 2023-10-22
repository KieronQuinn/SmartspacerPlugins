package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.appshortcuts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState.AppShortcuts
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app.AppPickerFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetExpandedAppShortcutsViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: AppShortcuts?)
    abstract fun dismiss()
    abstract fun onEnabledChanged(enabled: Boolean)
    abstract fun onAppClicked()
    abstract fun onAppChanged(packageName: String, label: String)
    abstract fun onCountChanged(count: Int)
    abstract fun onShowWhenLockedChanged(enabled: Boolean)

    sealed class State {
        object Loading: State()
        data class Loaded(val appShortcuts: AppShortcuts?): State()
    }

}

class TargetExpandedAppShortcutsViewModelImpl(
    private val navigation: ContainerNavigation
): TargetExpandedAppShortcutsViewModel() {

    private val appShortcuts = MutableStateFlow<AppShortcutsWrapper?>(null)

    override val state = appShortcuts.filterNotNull().mapLatest {
        State.Loaded(it.appShortcuts)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: AppShortcuts?) {
        if(appShortcuts.value != null) return
        viewModelScope.launch {
            appShortcuts.emit(AppShortcutsWrapper(current))
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled) {
                appShortcuts.emit(AppShortcutsWrapper(null))
            } else {
                appShortcuts.emit(AppShortcutsWrapper(AppShortcuts(null, null)))
            }
        }
    }

    override fun onAppClicked() {
        val current = appShortcuts.value?.appShortcuts?.packageName
        viewModelScope.launch {
            navigation.navigate(TargetExpandedAppShortcutsFragmentDirections.actionGlobalNavGraphIncludeApp(
                AppPickerFragment.Config(
                    TargetExpandedAppShortcutsFragment.REQUEST_KEY_APP,
                    current,
                    R.string.configuration_target_expanded_app_shortcuts_app_title
                )
            ))
        }
    }

    override fun onAppChanged(packageName: String, label: String) {
        val current = appShortcuts.value?.appShortcuts ?: return
        viewModelScope.launch {
            appShortcuts.emit(AppShortcutsWrapper(
                current.copy(packageName = packageName, label = label)
            ))
        }
    }

    override fun onCountChanged(count: Int) {
        val current = appShortcuts.value?.appShortcuts ?: return
        viewModelScope.launch {
            appShortcuts.emit(AppShortcutsWrapper(current.copy(appShortcutCount = count)))
        }
    }

    override fun onShowWhenLockedChanged(enabled: Boolean) {
        val current = appShortcuts.value?.appShortcuts ?: return
        viewModelScope.launch {
            appShortcuts.emit(AppShortcutsWrapper(current.copy(showWhenLocked = enabled)))
        }
    }

    data class AppShortcutsWrapper(val appShortcuts: AppShortcuts?)

}