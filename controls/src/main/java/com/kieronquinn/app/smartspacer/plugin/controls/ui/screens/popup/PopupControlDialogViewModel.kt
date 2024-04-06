package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.popup

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.service.controls.Control
import android.service.controls.actions.ControlAction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlState
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.getControlsIntent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getApplicationInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class PopupControlDialogViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract val interactionRequiredBus: Flow<InteractionRequired>

    abstract fun setup(
        smartspacerId: String,
        controlId: String,
        componentName: ComponentName
    )

    abstract fun onResume()
    abstract fun onPause()
    abstract fun onControlSetValue(newValue: Float)
    abstract fun onControlToggle()
    abstract fun onControlLongPress()
    abstract fun onPromptResult(tapAction: ControlTapAction, extraData: ControlExtraData)
    abstract fun onFabClicked()

    sealed class State {
        data object Loading: State()
        data class Loaded(
            val componentName: ComponentName,
            val appName: CharSequence,
            val controlState: ControlState,
            val controlsIntent: ControlsIntent
        ): State()
    }

    sealed class ControlsIntent {
        data class Intent(val intent: android.content.Intent): ControlsIntent()
        data object PowerMenu: ControlsIntent()
        data object None: ControlsIntent()
    }

    sealed class InteractionRequired(
        open val control: Control,
        open val controlAction: ControlTapAction,
        open val controlExtraData: ControlExtraData
    ) {
        data class Prompt(
            override val control: Control,
            override val controlAction: ControlTapAction,
            override val controlExtraData: ControlExtraData
        ): InteractionRequired(control, controlAction, controlExtraData)
        data class PIN(
            override val control: Control,
            override val controlAction: ControlTapAction,
            override val controlExtraData: ControlExtraData
        ): InteractionRequired(control, controlAction, controlExtraData)
        data class Password(
            override val control: Control,
            override val controlAction: ControlTapAction,
            override val controlExtraData: ControlExtraData
        ): InteractionRequired(control, controlAction, controlExtraData)
    }

}

class PopupControlDialogViewModelImpl(
    private val controlsRepository: ControlsRepository,
    context: Context
): PopupControlDialogViewModel() {

    private val config = MutableStateFlow<Config?>(null)

    private val control = config.filterNotNull().flatMapLatest {
        controlsRepository.getControlAsFlow(it.componentName, it.controlId, it.smartspacerId)
            .filterNotNull().stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                ControlState.Loading(
                    it.componentName,
                    it.controlId,
                    it.smartspacerId,
                    null,
                    null
                )
            )
    }

    private val appName = config.filterNotNull().mapLatest {
        try {
            context.packageManager.getApplicationInfo(it.componentName.packageName)
                .loadLabel(context.packageManager)
        }catch (e: NameNotFoundException) {
            ""
        }
    }

    private val controlsIntent = config.filterNotNull().mapLatest {
        val intent = controlsRepository.getPanelIntent(it.componentName)
            ?: context.getControlsIntent()
        if(intent != null) {
            return@mapLatest ControlsIntent.Intent(intent)
        }
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            ControlsIntent.PowerMenu
        }else ControlsIntent.None
    }

    override val state = combine(
        config.filterNotNull(),
        control,
        appName,
        controlsIntent
    ) { config, control, appName, controlsIntent ->
        State.Loaded(config.componentName, appName, control, controlsIntent)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override val interactionRequiredBus = MutableSharedFlow<InteractionRequired>()

    override fun setup(smartspacerId: String, controlId: String, componentName: ComponentName) {
        viewModelScope.launch {
            config.emit(Config(smartspacerId, controlId, componentName))
        }
    }

    override fun onResume() {
        controlsRepository.overrideListening(true)
    }

    override fun onPause() {
        controlsRepository.overrideListening(false)
    }

    override fun onControlToggle() {
        val currentState = (state.value as? State.Loaded)?.controlState ?: return
        val control = (currentState as? ControlState.Control)?.control ?: return
        controlsRepository.runControlTapAction(
            ControlTapAction.BOOLEAN,
            ControlExtraData(),
            control,
            currentState.componentName,
            currentState.smartspacerId
        ) { result, action, extra ->
            handleResult(result, action, extra)
        }
    }

    override fun onControlLongPress() {
        val currentState = (state.value as? State.Loaded)?.controlState ?: return
        val control = (currentState as? ControlState.Control)?.control ?: return
        controlsRepository.launchAppPendingIntent(control.appIntent)
    }

    override fun onPromptResult(tapAction: ControlTapAction, extraData: ControlExtraData) {
        val currentState = (state.value as? State.Loaded)?.controlState ?: return
        val control = (currentState as? ControlState.Control)?.control ?: return
        controlsRepository.runControlTapAction(
            tapAction,
            extraData,
            control,
            currentState.componentName,
            currentState.smartspacerId
        ) { result, action, extra ->
            handleResult(result, action, extra)
        }
    }

    override fun onFabClicked() {
        val currentState = state.value as? State.Loaded ?: return
        when(currentState.controlsIntent) {
            is ControlsIntent.Intent -> {
                controlsRepository.launchIntentAsRoot(currentState.controlsIntent.intent)
            }
            is ControlsIntent.PowerMenu -> {
                controlsRepository.showPowerMenu()
            }
            else -> {
                //No-op, shouldn't be visible
            }
        }
    }

    override fun onControlSetValue(newValue: Float) {
        val currentState = (state.value as? State.Loaded)?.controlState ?: return
        val control = (currentState as? ControlState.Control)?.control ?: return
        controlsRepository.runControlTapAction(
            ControlTapAction.FLOAT,
            ControlExtraData(floatSetFloat = newValue),
            control,
            currentState.componentName,
            currentState.smartspacerId
        ) { result, action, extra ->
            handleResult(result, action, extra)
        }
    }

    private fun handleResult(
        resultCode: Int,
        controlTapAction: ControlTapAction,
        controlExtraData: ControlExtraData
    ) {
        val currentState = (state.value as? State.Loaded)?.controlState ?: return
        val control = (currentState as? ControlState.Control)?.control ?: return
        val required = when(resultCode) {
            ControlAction.RESPONSE_CHALLENGE_ACK -> {
                InteractionRequired.Prompt(control, controlTapAction, controlExtraData)
            }
            ControlAction.RESPONSE_CHALLENGE_PASSPHRASE -> {
                InteractionRequired.Password(control, controlTapAction, controlExtraData)
            }
            ControlAction.RESPONSE_CHALLENGE_PIN -> {
                InteractionRequired.PIN(control, controlTapAction, controlExtraData)
            }
            else -> null
        } ?: return
        viewModelScope.launch {
            interactionRequiredBus.emit(required)
        }
    }

    data class Config(
        val smartspacerId: String,
        val controlId: String,
        val componentName: ComponentName
    )

}