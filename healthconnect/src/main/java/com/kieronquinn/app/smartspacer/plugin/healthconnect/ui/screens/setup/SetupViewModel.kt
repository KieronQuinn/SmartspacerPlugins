package com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.setup

import android.Manifest.permission.POST_NOTIFICATIONS
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.HealthConnectRepository
import com.kieronquinn.app.smartspacer.plugin.healthconnect.utils.extensions.getBatteryOptimisationIntent
import com.kieronquinn.app.smartspacer.plugin.healthconnect.utils.extensions.hasDisabledBatteryOptimisation
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SetupViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(dataType: DataType, smartspacerId: String, authority: String)
    abstract fun checkPermissions()
    abstract fun onOpenHealthConnectClicked()
    abstract fun onBatteryOptimisationClicked()
    abstract fun onNotificationsClicked(launcher: ActivityResultLauncher<String>)
    abstract fun showNotificationSettings()

    sealed class State {
        object Loading: State()
        data class RequestPermissions(
            val permissions: Set<String>,
            val hasGrantedHealthConnectPermissions: Boolean,
            val hasDisabledBatteryOptimisation: Boolean,
            val requiresNotificationPermission: Boolean,
            val hasEnabledNotifications: Boolean
        ): State()
        object Saving: State()
        data class Dismiss(val result: Int): State()
    }
    
}

class SetupViewModelImpl(
    context: Context,
    private val healthConnectRepository: HealthConnectRepository,
    private val dataRepository: DataRepository,
    private val navigation: ContainerNavigation
): SetupViewModel() {

    private val config = MutableStateFlow<Config?>(null)
    private val resumeBus = MutableStateFlow(System.currentTimeMillis())
    private val hasGrantedPermissions = MutableStateFlow(false)
    private val hasCommitted = MutableStateFlow(false)
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    private val hasDisabledBatteryOptimisation
        get() = powerManager.hasDisabledBatteryOptimisation()

    private val permissions = config.filterNotNull().map {
        setOf(healthConnectRepository.getPermission(it.dataType))
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val state = combine(
        hasGrantedPermissions,
        permissions.filterNotNull(),
        hasCommitted,
        resumeBus
    ) { granted, permissions, committed, _ ->
        val requiresNotificationPermission = Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU
        val hasGrantedNotificationPermission = !requiresNotificationPermission ||
                context.hasNotificationPermission()
        val hasFinished = granted && hasGrantedNotificationPermission &&
                hasDisabledBatteryOptimisation
        when {
            committed -> State.Dismiss(Activity.RESULT_OK)
            hasFinished -> State.Saving
            else -> State.RequestPermissions(
                permissions,
                granted,
                hasDisabledBatteryOptimisation,
                requiresNotificationPermission,
                hasGrantedNotificationPermission
            )
        }
    }.onEach {
        when (it) {
            is State.Saving -> {
                saveConfig()
            }
            else -> {
                //No-op
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(dataType: DataType, smartspacerId: String, authority: String) {
        viewModelScope.launch {
            config.emit(Config(dataType, smartspacerId, authority))
        }
    }

    override fun checkPermissions() {
        val permissions = permissions.value ?: return
        viewModelScope.launch {
            val granted = permissions.all {
                healthConnectRepository.hasPermission(it)
            }
            hasGrantedPermissions.emit(granted)
            resumeBus.emit(System.currentTimeMillis())
        }
    }

    override fun onOpenHealthConnectClicked() {
        viewModelScope.launch {
            navigation.navigate(healthConnectRepository.getOpenHealthConnectIntent())
        }
    }

    @SuppressLint("BatteryLife")
    override fun onBatteryOptimisationClicked() {
        viewModelScope.launch {
            navigation.navigate(getBatteryOptimisationIntent())
        }
    }

    override fun showNotificationSettings() {
        viewModelScope.launch {
            navigation.navigate(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, BuildConfig.APPLICATION_ID)
            })
        }
    }

    @SuppressLint("InlinedApi")
    override fun onNotificationsClicked(launcher: ActivityResultLauncher<String>) {
        launcher.launch(POST_NOTIFICATIONS)
    }

    private fun saveConfig() {
        val config = config.value ?: return
        dataRepository.updateComplicationData(
            config.smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onDataSaved
        ) {
            it ?: ComplicationData(config.dataType)
        }
    }

    private fun onDataSaved(context: Context, smartspacerId: String) = viewModelScope.launch {
        val config = config.value ?: return@launch
        SmartspacerComplicationProvider.notifyChange(context, config.authority, smartspacerId)
        healthConnectRepository.updateHealthMetric(smartspacerId, config.authority)
        hasCommitted.emit(true)
    }

    private fun Context.hasNotificationPermission(): Boolean {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return checkCallingOrSelfPermission(POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private data class Config(
        val dataType: DataType,
        val smartspacerId: String,
        val authority: String
    )

}