package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.requirement

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
import com.kieronquinn.app.smartspacer.plugin.controls.model.LoadingConfig
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ShizukuServiceRepository
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData.RequirementType
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData.RequirementValueType
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.control.ControlPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.hasPermission
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerRequirementProvider
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

abstract class RequirementConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(smartspacerId: String)
    abstract fun onResume()

    abstract fun onSelectControlClicked()
    abstract fun onControlChanged(control: ControlPickerViewModel.Control)
    abstract fun onNotificationPermissionClicked(launcher: ActivityResultLauncher<String>)
    abstract fun onShowAppInfo()
    abstract fun onDisableBatteryOptimisationClicked()
    abstract fun onLoadingConfigChanged(config: LoadingConfig)
    abstract fun onRequirementTypeChanged(type: RequirementType)
    abstract fun onRequirementValueTypeChanged(type: RequirementValueType)
    abstract fun onBooleanValueChanged(value: Boolean)
    abstract fun onModeValueChanged(mode: ControlMode)
    abstract fun onFloatValueChanged(value: Float)

    sealed class State {
        data object Loading: State()
        data object Incompatible: State()
        data class Loaded(
            val hasNotificationPermission: Boolean,
            val hasDisabledBatteryOptimisation: Boolean,
            val data: RequirementData,
            val control: Control?
        ): State()
    }

}

class RequirementConfigurationViewModelImpl(
    context: Context,
    private val dataRepository: DataRepository,
    private val navigation: ContainerNavigation,
    private val controlsRepository: ControlsRepository,
    shizukuServiceRepository: ShizukuServiceRepository
): RequirementConfigurationViewModel() {

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
        dataRepository.getRequirementDataFlow(id, RequirementData::class.java).mapLatest {
            it ?: RequirementData(id)
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
    ) { compatible, hasPermission, hasDisabledBatteryOptimisation, requirementData, control ->
        when {
            !compatible -> State.Incompatible
            control is ControlState.Loaded -> {
                State.Loaded(
                    hasPermission,
                    hasDisabledBatteryOptimisation,
                    requirementData,
                    control.control
                )
            }
            else -> State.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(smartspacerId: String) {
        viewModelScope.launch {
            this@RequirementConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onResume() {
        viewModelScope.launch {
            resumeBus.emit(System.currentTimeMillis())
        }
    }

    override fun onSelectControlClicked() {
        viewModelScope.launch {
            navigation.navigate(RequirementConfigurationFragmentDirections
                .actionRequirementConfigurationFragmentToNavGraphAppPicker())
        }
    }

    override fun onControlChanged(control: ControlPickerViewModel.Control) {
        updateData {
            //We don't copy to prevent saving now-invalid configurations
            RequirementData(
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

    override fun onRequirementTypeChanged(type: RequirementType) {
        updateData {
            copy(
                requirementType = type,
                boolean = null,
                mode = null,
                float = null,
                floatType = null
            )
        }
    }

    override fun onBooleanValueChanged(value: Boolean) {
        updateData {
            copy(boolean = value)
        }
    }

    override fun onFloatValueChanged(value: Float) {
        updateData {
            copy(float = value)
        }
    }

    override fun onModeValueChanged(mode: ControlMode) {
        updateData {
            copy(mode = mode)
        }
    }

    override fun onRequirementValueTypeChanged(type: RequirementValueType) {
        updateData {
            copy(floatType = type)
        }
    }

    private fun updateData(block: RequirementData.() -> RequirementData) {
        val id = smartspacerId.value ?: return
        dataRepository.updateRequirementData(
            id,
            RequirementData::class.java,
            RequirementData.TYPE,
            ::onUpdated
        ) {
            val current = it ?: RequirementData(id)
            block(current)
        }
    }

    private fun onUpdated(context: Context, smartspacerId: String) {
        SmartspacerRequirementProvider.notifyChange(
            context, ControlsRequirement::class.java, smartspacerId
        )
    }

    private fun loadControl(data: RequirementData?) = flow {
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