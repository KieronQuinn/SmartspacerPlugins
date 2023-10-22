package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction

import android.text.InputType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app.AppPickerFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.InputValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TapActionViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: TapAction?)
    abstract fun dismiss()
    abstract fun onLaunchAppClicked(key: String)
    abstract fun onOpenUrlClicked(key: String, current: TapAction.Url?)
    abstract fun onTaskerEventClicked(key: String, current: TapAction.TaskerEvent?)

    sealed class State {
        object Loading: State()
        data class Loaded(val tapAction: TapAction?): State()
    }

}

class TapActionViewModelImpl(private val navigation: ContainerNavigation): TapActionViewModel() {

    private val tapAction = MutableStateFlow<TapActionWrapper?>(null)

    override val state = tapAction.filterNotNull().mapLatest {
        State.Loaded(it.tapAction)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: TapAction?) {
        if(tapAction.value != null) return
        viewModelScope.launch {
            tapAction.emit(TapActionWrapper(current))
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onLaunchAppClicked(key: String) {
        val current = tapAction.value?.tapAction as? TapAction.LaunchApp
        viewModelScope.launch {
            navigation.navigate(TapActionFragmentDirections.actionTapActionFragmentToNavGraphIncludeApp(
                AppPickerFragment.Config(
                    key,
                    current?.packageName,
                    R.string.tap_action_launch_app_title
                )
            ))
        }
    }

    override fun onOpenUrlClicked(key: String, current: TapAction.Url?) {
        viewModelScope.launch {
            navigation.navigate(TapActionFragmentDirections.actionTapActionFragmentToNavGraphIncludeString(
                StringInputFragment.Config(
                    current?.url ?: "",
                    key,
                    R.string.tap_action_open_url_title,
                    R.string.tap_action_open_url_content_dialog,
                    R.string.tap_action_open_url_hint,
                    inputValidation = InputValidation.URL,
                    inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS
                )
            ))
        }
    }

    override fun onTaskerEventClicked(key: String, current: TapAction.TaskerEvent?) {
        viewModelScope.launch {
            navigation.navigate(TapActionFragmentDirections.actionTapActionFragmentToNavGraphIncludeString(
                StringInputFragment.Config(
                    current?.id ?: "",
                    key,
                    R.string.tap_action_trigger_tasker_event_dialog_title,
                    R.string.tap_action_trigger_tasker_event_dialog_content,
                    R.string.tap_action_trigger_tasker_event_dialog_title,
                    inputValidation = InputValidation.NOT_EMPTY,
                    inputType = InputType.TYPE_CLASS_TEXT
                )
            ))
        }
    }

    data class TapActionWrapper(val tapAction: TapAction?)

}