package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.target

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.service.controls.Control
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.controls.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlMode
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.model.Icon
import com.kieronquinn.app.smartspacer.plugin.controls.model.LoadingConfig
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ShizukuServiceRepository
import com.kieronquinn.app.smartspacer.plugin.controls.targets.ControlsTarget
import com.kieronquinn.app.smartspacer.plugin.controls.targets.ControlsTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.icon.IconPickerFragment
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.control.ControlPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.hasPermission
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(smartspacerId: String)
    abstract fun onResume()

    abstract fun onSelectControlClicked()
    abstract fun onControlChanged(control: ControlPickerViewModel.Control)
    abstract fun onNotificationPermissionClicked(launcher: ActivityResultLauncher<String>)
    abstract fun onShowAppInfo()
    abstract fun onDisableBatteryOptimisationClicked()
    abstract fun onLoadingConfigChanged(config: LoadingConfig)
    abstract fun onTapActionChanged(action: ControlTapAction)
    abstract fun onRequireUnlockChanged(enabled: Boolean)
    abstract fun onFloatValueChanged(value: Float)
    abstract fun onModeChanged(mode: ControlMode)
    abstract fun onHideDetailsChanged(enabled: Boolean)
    abstract fun onIconClicked(key: String)
    abstract fun onIconChanged(icon: Icon?)
    abstract fun onCustomTitleClicked()
    abstract fun onCustomTitleChanged(title: String?)
    abstract fun onCustomSubtitleClicked()
    abstract fun onCustomSubtitleChanged(subtitle: String?)
    abstract fun onSettingsClicked()

    sealed class State {
        data object Loading: State()
        data object Incompatible: State()
        data class Loaded(
            val hasNotificationPermission: Boolean,
            val hasDisabledBatteryOptimisation: Boolean,
            val data: TargetData,
            val control: Control?,
            val panelAvailable: Boolean
        ): State()
    }

}

class TargetConfigurationViewModelImpl(
    context: Context,
    private val dataRepository: DataRepository,
    private val navigation: ContainerNavigation,
    private val controlsRepository: ControlsRepository,
    shizukuServiceRepository: ShizukuServiceRepository
): TargetConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)
    private val resumeBus = MutableStateFlow(System.currentTimeMillis())
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val isCompatible = shizukuServiceRepository.isReady.mapLatest {
        shizukuServiceRepository.runWithService { service ->
            service.isCompatible
        }.unwrap() ?: false
    }

    private val data = smartspacerId.flatMapLatest { id ->
        if(id == null) return@flatMapLatest flowOf(null)
        dataRepository.getTargetDataFlow(id, TargetData::class.java).mapLatest {
            it ?: TargetData(id)
        }
    }.distinctUntilChanged { old, new ->
        //We need to ignore changing of float values here so we don't reset the slider
        old?.equalsIgnoringFloat(new) == true
    }

    private val control = data.filterNotNull().distinctUntilChanged { old, new ->
        old.controlId == new.controlId && old.componentName == new.componentName
    }.flatMapLatest {
        loadControl(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ControlState.Loading)

    private val hasNotificationPermission = resumeBus.map {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else true
    }

    private val hasDisabledBatteryOptimisation = resumeBus.map {
        powerManager.isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
    }

    override val state = combine(
        isCompatible,
        hasNotificationPermission,
        hasDisabledBatteryOptimisation,
        data.filterNotNull(),
        control
    ) { compatible, hasPermission, hasDisabledBatteryOptimisation, targetData, control ->
        val panelAvailable = targetData.componentName?.let {
            controlsRepository.getPanelIntent(it)
        } != null
        when {
            !compatible -> State.Incompatible
            control is ControlState.Loaded -> {
                State.Loaded(
                    hasPermission,
                    hasDisabledBatteryOptimisation,
                    targetData,
                    control.control,
                    panelAvailable
                )
            }
            else -> State.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(smartspacerId: String) {
        viewModelScope.launch {
            this@TargetConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onResume() {
        viewModelScope.launch {
            resumeBus.emit(System.currentTimeMillis())
        }
    }

    override fun onSelectControlClicked() {
        viewModelScope.launch {
            navigation.navigate(TargetConfigurationFragmentDirections
                .actionTargetConfigurationFragmentToNavGraphAppPicker())
        }
    }

    override fun onControlChanged(control: ControlPickerViewModel.Control) {
        updateData {
            //We don't copy to prevent saving now-invalid configurations
            TargetData(
                smartspacerId = smartspacerId,
                controlComponentName = control.componentName.flattenToString(),
                controlApp = control.providerName,
                controlId = control.controlId,
                controlName = control.label
            )
        }
    }

    override fun onNotificationPermissionClicked(launcher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onShowAppInfo() {
        viewModelScope.launch {
            navigation.navigate(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            })
        }
    }

    @SuppressLint("BatteryLife")
    override fun onDisableBatteryOptimisationClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
            })
        }
    }

    override fun onLoadingConfigChanged(config: LoadingConfig) {
        updateData {
            copy(loadingConfig = config)
        }
    }

    override fun onTapActionChanged(action: ControlTapAction) {
        updateData {
            if(tapAction == action) return@updateData this //Don't overwrite identical picked
            copy(tapAction = action, modeSetMode = null, floatSetFloat = null)
        }
    }

    override fun onRequireUnlockChanged(enabled: Boolean) {
        updateData {
            copy(requiresUnlock = enabled)
        }
    }

    override fun onFloatValueChanged(value: Float) {
        updateData {
            copy(floatSetFloat = value)
        }
    }

    override fun onModeChanged(mode: ControlMode) {
        updateData {
            copy(modeSetMode = mode)
        }
    }

    override fun onHideDetailsChanged(enabled: Boolean) {
        updateData {
            copy(hideDetails = enabled)
        }
    }

    override fun onIconClicked(key: String) {
        viewModelScope.launch {
            val current = (state.value as? State.Loaded)?.data?.customIcon
            navigation.navigate(
                TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToNavGraphIconPicker(
                    IconPickerFragment.Config(key, current)
                )
            )
        }
    }

    override fun onIconChanged(icon: Icon?) {
        updateData {
            copy(customIcon = icon)
        }
    }

    override fun onCustomTitleClicked() {
        viewModelScope.launch {
            val current = (state.value as? State.Loaded)?.data?.customTitle
            navigation.navigate(
                TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToNavGraphCustomTitle(
                    current ?: ""
                )
            )
        }
    }

    override fun onCustomTitleChanged(title: String?) {
        updateData {
            copy(customTitle = title)
        }
    }

    override fun onCustomSubtitleClicked() {
        viewModelScope.launch {
            val current = (state.value as? State.Loaded)?.data?.customSubtitle
            navigation.navigate(
                TargetConfigurationFragmentDirections.actionTargetConfigurationFragmentToCustomSubtitleBottomSheetFragment(
                    current ?: ""
                )
            )
        }
    }

    override fun onCustomSubtitleChanged(subtitle: String?) {
        updateData {
            copy(customSubtitle = subtitle)
        }
    }

    override fun onSettingsClicked() {
        viewModelScope.launch {
            navigation.navigate(
                TargetConfigurationFragmentDirections
                    .actionTargetConfigurationFragmentToNavGraphSettings()
            )
        }
    }

    private fun updateData(block: TargetData.() -> TargetData) {
        val id = smartspacerId.value ?: return
        dataRepository.updateTargetData(
            id,
            TargetData::class.java,
            TargetData.TYPE,
            ::onUpdated
        ) {
            val current = it ?: TargetData(id)
            block(current)
        }
    }

    private fun onUpdated(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(
            context, ControlsTarget::class.java, smartspacerId
        )
    }

    private fun loadControl(data: TargetData?) = flow {
        emit(ControlState.Loading)
        val controlId = data?.controlId
        val componentName = data?.componentName
        if(controlId == null || componentName == null) {
            emit(ControlState.Loaded(null))
            return@flow
        }
        controlsRepository.subscribeOnce(componentName, controlId, data.smartspacerId).collect {
            emit(ControlState.Loaded(it))
        }
    }

    sealed class ControlState {
        data object Loading: ControlState()
        data class Loaded(val control: Control?): ControlState()
    }

}