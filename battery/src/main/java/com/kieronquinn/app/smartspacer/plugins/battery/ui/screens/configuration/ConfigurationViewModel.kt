package com.kieronquinn.app.smartspacer.plugins.battery.ui.screens.configuration

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugins.battery.complications.BatteryComplication
import com.kieronquinn.app.smartspacer.plugins.battery.complications.BatteryComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugins.battery.model.BatteryLevels
import com.kieronquinn.app.smartspacer.plugins.battery.model.BatteryLevels.BatteryLevel
import com.kieronquinn.app.smartspacer.plugins.battery.repositories.BatteryRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class ConfigurationViewModel: ViewModel() {

    abstract val dismissBus: Flow<Unit>
    abstract val state: StateFlow<State>

    abstract fun setup(smartspacerId: String)
    abstract fun onDeviceClicked(batteryLevel: BatteryLevel)

    sealed class State {
        data object Loading: State()
        data class Loaded(
            val complicationData: ComplicationData,
            val batteryLevels: BatteryLevels?
        ): State()
    }

}

class ConfigurationViewModelImpl(
    private val dataRepository: DataRepository,
    private val batteryRepository: BatteryRepository
): ConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val complicationData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getComplicationDataFlow(it, ComplicationData::class.java)
    }

    private val batteryLevels = batteryRepository.batteryLevelsChanged.mapLatest {
        batteryRepository.getBatteryLevels()
    }.flowOn(Dispatchers.IO)

    override val dismissBus = MutableSharedFlow<Unit>()

    override val state = combine(
        complicationData,
        batteryLevels
    ) { complication, battery ->
        State.Loaded(complication ?: ComplicationData(), battery)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(smartspacerId: String) {
        viewModelScope.launch {
            this@ConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onDeviceClicked(batteryLevel: BatteryLevel) {
        val smartspacerId = smartspacerId.value ?: return
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onUpdated
        ) {
            val data = it ?: ComplicationData()
            data.copy(name = batteryLevel.name)
        }
    }

    private fun onUpdated(context: Context, smartspacerId: String) {
        SmartspacerComplicationProvider.notifyChange(
            context, BatteryComplication::class.java, smartspacerId
        )
        viewModelScope.launch {
            dismissBus.emit(Unit)
        }
    }

}