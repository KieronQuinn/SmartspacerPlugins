package com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.configuration

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.RefreshPeriod
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.TimeoutPeriod
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.UnitType
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.database.HealthData
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.HealthConnectRepository
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

abstract class ConfigurationViewModel: ViewModel() {

    abstract val refreshCompleteBus: Flow<Boolean>
    abstract val state: StateFlow<State>

    abstract fun setup(smartspacerId: String, authority: String)
    abstract fun checkPermissions()
    abstract fun onPermissionClicked()
    abstract fun onUnitTypeChanged(unitType: UnitType)
    abstract fun onResetTimeChanged(time: LocalTime)
    abstract fun onTimeoutChanged(timeoutPeriod: TimeoutPeriod)
    abstract fun onRefreshPeriodChanged(refreshPeriod: RefreshPeriod)
    abstract fun onRefreshClicked()

    sealed class State {
        object Loading: State()
        data class Loaded(
            val config: ComplicationData,
            val permissionsRequired: Set<Int>,
            val isRefreshing: Boolean,
            val healthData: HealthData?
        ): State()
    }

}

class ConfigurationViewModelImpl(
    private val dataRepository: DataRepository,
    private val navigation: ContainerNavigation,
    private val healthConnectRepository: HealthConnectRepository,
    databaseRepository: DatabaseRepository
): ConfigurationViewModel() {

    private val smartspacerConfig = MutableStateFlow<SmartspacerConfig?>(null)
    private val resumeBus = MutableStateFlow(System.currentTimeMillis())
    private val isRefreshing = MutableStateFlow(false)

    private val config = smartspacerConfig.filterNotNull().flatMapLatest {
        dataRepository.getComplicationDataFlow(it.smartspacerId, ComplicationData::class.java)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val permissions = combine(
        config.filterNotNull(),
        resumeBus
    ) { it, _ ->
        listOf(it.dataType).filterNot { d ->
            val permission = healthConnectRepository.getPermission(d)
            healthConnectRepository.hasPermission(permission)
        }.map {
            it.label
        }.toSet()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val healthData = smartspacerConfig.flatMapLatest {
        val smartspacerId = it?.smartspacerId ?: return@flatMapLatest flowOf(null)
        databaseRepository.getHealthDataAsFlow(smartspacerId)
    }

    override val refreshCompleteBus = MutableSharedFlow<Boolean>()

    override val state = combine(
        config.filterNotNull(),
        permissions.filterNotNull(),
        isRefreshing,
        healthData
    ) { c, p, r, d ->
        State.Loaded(c, p, r, d)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(smartspacerId: String, authority: String) {
        viewModelScope.launch {
            smartspacerConfig.emit(SmartspacerConfig(smartspacerId, authority))
        }
    }

    override fun checkPermissions() {
        viewModelScope.launch {
            resumeBus.emit(System.currentTimeMillis())
        }
    }

    override fun onPermissionClicked() {
        viewModelScope.launch {
            navigation.navigate(healthConnectRepository.getOpenHealthConnectIntent())
        }
    }

    override fun onUnitTypeChanged(unitType: UnitType) {
        updateComplicationData {
            copy(unitType = unitType.name)
        }
    }

    override fun onResetTimeChanged(time: LocalTime) {
        updateComplicationData {
            copy(_resetTime = time.format(DateTimeFormatter.ISO_LOCAL_TIME))
        }
    }

    override fun onTimeoutChanged(timeoutPeriod: TimeoutPeriod) {
        updateComplicationData {
            copy(timeout = timeoutPeriod)
        }
    }

    override fun onRefreshPeriodChanged(refreshPeriod: RefreshPeriod) {
        updateComplicationData {
            copy(refreshPeriod = refreshPeriod)
        }
    }

    private fun updateComplicationData(block: ComplicationData.() -> ComplicationData) {
        val smartspacerId = smartspacerConfig.value?.smartspacerId ?: return
        val dataType = (state.value as? State.Loaded)?.config?.dataType ?: return
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::updateHealthMetric
        ) {
            val data = it ?: ComplicationData(dataType)
            block(data)
        }
    }

    override fun onRefreshClicked() {
        val smartspacerId = smartspacerConfig.value?.smartspacerId ?: return
        val authority = smartspacerConfig.value?.authority ?: return
        updateHealthMetric(smartspacerId, authority, true)
    }

    private fun updateHealthMetric(context: Context, smartspacerId: String) {
        val authority = smartspacerConfig.value?.authority ?: return
        updateHealthMetric(smartspacerId, authority, false)
    }

    private fun updateHealthMetric(smartspacerId: String, authority: String, showToast: Boolean) {
        viewModelScope.launch {
            isRefreshing.emit(true)
            val result = healthConnectRepository.updateHealthMetric(smartspacerId, authority)
            isRefreshing.emit(false)
            if(showToast) {
                refreshCompleteBus.emit(result)
            }
        }
    }

    data class SmartspacerConfig(val smartspacerId: String, val authority: String)

}