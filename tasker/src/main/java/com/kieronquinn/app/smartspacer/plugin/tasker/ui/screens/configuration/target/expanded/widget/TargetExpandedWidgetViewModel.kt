package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget

import android.text.InputType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState.Widget
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.picker.TargetExpandedWidgetPickerFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetExpandedWidgetViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: Widget?)
    abstract fun dismiss()
    abstract fun onEnabledChanged(enabled: Boolean)
    abstract fun onComponentNameClicked()
    abstract fun onComponentNameChanged(componentName: String, label: String)
    abstract fun onIdClicked()
    abstract fun onIdChanged(id: String)
    abstract fun onShowWhenLockedChanged(enabled: Boolean)
    abstract fun onSkipConfigurationChanged(enabled: Boolean)
    abstract fun onWidthClicked()
    abstract fun onWidthChanged(width: String)
    abstract fun onHeightClicked()
    abstract fun onHeightChanged(height: String)

    sealed class State {
        object Loading: State()
        data class Loaded(val widget: Widget?): State()
    }

}

class TargetExpandedWidgetViewModelImpl(
    private val navigation: ContainerNavigation
): TargetExpandedWidgetViewModel() {

    private val widget = MutableStateFlow<WidgetWrapper?>(null)

    override val state = widget.filterNotNull().map {
        State.Loaded(it.widget)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: Widget?) {
        if(widget.value != null) return
        viewModelScope.launch {
            widget.emit(WidgetWrapper(current))
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            if(enabled) {
                widget.emit(WidgetWrapper(null))
            }else{
                widget.emit(WidgetWrapper(Widget()))
            }
        }
    }

    override fun onComponentNameClicked() {
        val current = widget.value?.widget?._componentName
        viewModelScope.launch {
            navigation.navigate(TargetExpandedWidgetFragmentDirections.actionTargetExpandedWidgetFragmentToTargetExpandedWidgetPickerFragment(
                TargetExpandedWidgetPickerFragment.Config(
                    TargetExpandedWidgetFragment.REQUEST_KEY_WIDGET,
                    current
                )
            ))
        }
    }

    override fun onComponentNameChanged(componentName: String, label: String) {
        updateWidget {
            copy(_componentName = componentName, label = label)
        }
    }

    override fun onIdClicked() {
        val current = widget.value?.widget?.id ?: ""
        viewModelScope.launch {
            navigation.navigate(TargetExpandedWidgetFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    current,
                    TargetExpandedWidgetFragment.REQUEST_KEY_ID,
                    R.string.configuration_target_expanded_widget_id_title,
                    R.string.configuration_target_expanded_widget_id_description,
                    R.string.configuration_target_expanded_widget_id_title,
                    inputValidation = StringInputFragment.InputValidation.NOT_EMPTY,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onIdChanged(id: String) {
        updateWidget {
            copy(id = id)
        }
    }

    override fun onShowWhenLockedChanged(enabled: Boolean) {
        updateWidget {
            copy(showWhenLocked = enabled)
        }
    }

    override fun onSkipConfigurationChanged(enabled: Boolean) {
        updateWidget {
            copy(skipConfigure = enabled)
        }
    }

    override fun onWidthClicked() {
        val current = widget.value?.widget?._width ?: ""
        viewModelScope.launch {
            navigation.navigate(TargetExpandedWidgetFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    current,
                    TargetExpandedWidgetFragment.REQUEST_KEY_WIDTH,
                    R.string.configuration_target_expanded_widget_width_title,
                    R.string.configuration_target_expanded_widget_width_description,
                    R.string.configuration_target_expanded_widget_width_title,
                    suffix = R.string.input_string_suffix_pixels,
                    inputValidation = StringInputFragment.InputValidation.WIDTH,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onWidthChanged(width: String) {
        updateWidget {
            copy(_width = width)
        }
    }

    override fun onHeightClicked() {
        val current = widget.value?.widget?._height ?: ""
        viewModelScope.launch {
            navigation.navigate(TargetExpandedWidgetFragmentDirections.actionGlobalNavGraphIncludeString(
                StringInputFragment.Config(
                    current,
                    TargetExpandedWidgetFragment.REQUEST_KEY_HEIGHT,
                    R.string.configuration_target_expanded_widget_height_title,
                    R.string.configuration_target_expanded_widget_height_description,
                    R.string.configuration_target_expanded_widget_height_title,
                    suffix = R.string.input_string_suffix_pixels,
                    inputValidation = StringInputFragment.InputValidation.HEIGHT,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                )
            ))
        }
    }

    override fun onHeightChanged(height: String) {
        updateWidget {
            copy(_height = height)
        }
    }

    private fun updateWidget(block: Widget.() -> Widget) {
        viewModelScope.launch {
            widget.emit(WidgetWrapper(block(widget.value?.widget ?: return@launch)))
        }
    }

    data class WidgetWrapper(val widget: Widget?)

}