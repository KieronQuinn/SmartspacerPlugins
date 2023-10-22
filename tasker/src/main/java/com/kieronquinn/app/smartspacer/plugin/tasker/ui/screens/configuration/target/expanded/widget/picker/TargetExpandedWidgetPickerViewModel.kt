package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.WidgetRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSearchViewModel
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.InputValidation
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.isVariable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetExpandedWidgetPickerViewModel: ViewModel(), BaseSearchViewModel {

    abstract val state: StateFlow<State>

    abstract fun dismiss()
    abstract fun onVariableClicked(current: String?)

    sealed class Item {
        data class App(val label: String): Item()
        data class Widget(val widget: WidgetRepository.Widget): Item()
    }

    sealed class State {
        object Loading: State()
        data class Loaded(val items: List<Item>): State()
    }

}

class TargetExpandedWidgetPickerViewModelImpl(
    private val navigation: ContainerNavigation,
    widgetRepository: WidgetRepository
): TargetExpandedWidgetPickerViewModel() {

    private val searchTerm = MutableStateFlow("")

    override val state = combine(
        widgetRepository.getWidgets(),
        searchTerm
    ) { widgets, search ->
        widgets.filter(search).flatMap {
            listOf(
                Item.App(it.label),
                *it.widgets.map { w -> Item.Widget(w) }.toTypedArray()
            )
        }.let {
            State.Loaded(it)
        }
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override val showSearchClear = searchTerm.mapLatest {
        it.isNotBlank()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private fun List<WidgetRepository.WidgetApp>.filter(
        searchTerm: String
    ): List<WidgetRepository.WidgetApp> {
        return filter {
            it.label.contains(searchTerm, true) || it.widgets.any { widget ->
                widget.label.contains(searchTerm, true)
            }
        }.map {
            if(it.label.contains(searchTerm, true)) return@map it
            it.copy(widgets = it.widgets.filter { w ->
                w.label.contains(searchTerm, true)
            })
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onVariableClicked(current: String?) {
        viewModelScope.launch {
            navigation.navigate(TargetExpandedWidgetPickerFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    current?.takeIf { it.isVariable() } ?: "",
                    TargetExpandedWidgetPickerFragment.REQUEST_KEY_VARIABLE,
                    R.string.configuration_target_expanded_widget_title,
                    R.string.configuration_target_expanded_widget_picker_variable_content,
                    R.string.configuration_target_expanded_widget_picker_variable_hint,
                    inputValidation = InputValidation.TASKER_VARIABLE
                )
            ))
        }
    }

    override fun setSearchTerm(term: String) {
        viewModelScope.launch {
            searchTerm.emit(term)
        }
    }

    override fun getSearchTerm(): String {
        return searchTerm.value
    }

}