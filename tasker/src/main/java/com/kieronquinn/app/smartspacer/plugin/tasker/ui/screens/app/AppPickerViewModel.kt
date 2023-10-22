package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.PackageRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.PackageRepository.ListAppsApp
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.InputValidation
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.isVariable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.jetbrains.annotations.VisibleForTesting

abstract class AppPickerViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract val showSearchClear: StateFlow<Boolean>

    abstract fun dismiss()
    abstract fun setSearchTerm(term: String)
    abstract fun getSearchTerm(): String
    abstract fun onVariableClicked(current: String?)

    sealed class State {
        object Loading: State()
        data class Loaded(val apps: List<ListAppsApp>): State()
    }

}

class AppPickerViewModelImpl(
    private val navigation: ContainerNavigation,
    packageRepository: PackageRepository,
    includeNotLaunchable: Boolean
): AppPickerViewModel() {

    private val allApps = flow {
        emit(packageRepository.getInstalledApps(includeNotLaunchable))
    }.flowOn(Dispatchers.IO)

    @VisibleForTesting
    val searchTerm = MutableStateFlow("")

    override val showSearchClear = searchTerm.map { it.isNotBlank() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    override val state = combine(allApps, searchTerm) { all, term ->
        State.Loaded(all.filter {
            it.label.contains(term, true) || it.packageName.contains(term, true)
        })
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateUpTo(R.id.nav_graph_include_app, true)
        }
    }

    override fun getSearchTerm(): String {
        return searchTerm.value
    }

    override fun setSearchTerm(term: String) {
        viewModelScope.launch {
            searchTerm.emit(term)
        }
    }

    override fun onVariableClicked(current: String?) {
        viewModelScope.launch {
            navigation.navigate(AppPickerFragmentDirections.actionAppPickerFragmentToNavGraphIncludeString(
                StringInputFragment.Config(
                    current?.takeIf { it.isVariable() } ?: "",
                    AppPickerFragment.REQUEST_KEY_VARIABLE,
                    R.string.tap_action_launch_app_title,
                    R.string.tap_action_launch_app_variable_content,
                    R.string.tap_action_launch_app_variable_hint,
                    inputValidation = InputValidation.TASKER_VARIABLE
                )
            ))
        }
    }

}