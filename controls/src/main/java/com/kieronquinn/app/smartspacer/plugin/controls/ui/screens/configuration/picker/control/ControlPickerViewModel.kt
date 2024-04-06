package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.control

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates.ControlsTemplate
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlsApp
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

abstract class ControlPickerViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract fun setup(controlsApp: ControlsApp)
    abstract fun dismiss()

    sealed class State {
        data object Loading: State()
        data object Error: State()
        data class Controls(val controls: List<Control>): State()
    }

    @Parcelize
    data class Control(
        val componentName: ComponentName,
        val providerName: String,
        val label: String,
        val subtitle: String,
        val controlId: String,
        @IgnoredOnParcel
        val icon: Drawable? = null
    ): Parcelable

}

class ControlPickerViewModelImpl(
    private val navigation: ContainerNavigation,
    context: Context,
    controlsRepository: ControlsRepository
): ControlPickerViewModel() {

    private val controlsApp = MutableStateFlow<ControlsApp?>(null)

    private val controls = controlsApp.filterNotNull().mapLatest {
        controlsRepository.getControlsForApp(it.componentName)
    }.flowOn(Dispatchers.IO)

    override val state = combine(
        controlsApp.filterNotNull(),
        controls
    ) { app, all ->
        val controls = all?.map { control ->
            val template = ControlsTemplate.getTemplate(control.controlTemplate)
            val icon = template.run {
                control.getIcon(context, app.componentName)
            }
            Control(
                app.componentName,
                app.name.toString(),
                control.title.toString(),
                control.subtitle.toString(),
                control.controlId,
                icon.loadDrawable(context)
            )
        } ?: return@combine State.Error
        State.Controls(controls)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(controlsApp: ControlsApp) {
        viewModelScope.launch {
            this@ControlPickerViewModelImpl.controlsApp.emit(controlsApp)
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateUpTo(R.id.nav_graph_app_picker, true)
        }
    }

}