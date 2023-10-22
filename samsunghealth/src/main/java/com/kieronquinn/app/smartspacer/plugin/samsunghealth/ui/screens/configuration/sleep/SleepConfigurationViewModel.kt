package com.kieronquinn.app.smartspacer.plugin.samsunghealth.ui.screens.configuration.sleep

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.complications.SleepComplication
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.complications.SleepComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.complications.SleepComplication.ComplicationData.Timeout
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SleepConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setupWithId(smartspacerId: String)
    abstract fun onTimeoutEnabledChanged(enabled: Boolean)
    abstract fun onTimeoutChanged(timeout: Timeout)

    sealed class State {
        object Loading: State()
        data class Loaded(val timeoutEnabled: Boolean, val timeout: Timeout): State()
    }

}

class SleepConfigurationViewModelImpl(
    private val dataRepository: DataRepository
): SleepConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val complicationData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getComplicationDataFlow(it, ComplicationData::class.java).map { data ->
            data ?: ComplicationData()
        }
    }

    override val state = complicationData.mapLatest {
        State.Loaded(it.timeoutEnabled, it.timeout)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setupWithId(smartspacerId: String) {
        viewModelScope.launch {
            this@SleepConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onTimeoutChanged(timeout: Timeout) {
        val smartspacerId = smartspacerId.value ?: return
        updateConfiguration(smartspacerId, timeout = timeout)
    }

    override fun onTimeoutEnabledChanged(enabled: Boolean) {
        val smartspacerId = smartspacerId.value ?: return
        updateConfiguration(smartspacerId, timeoutEnabled = enabled)
    }

    private fun updateConfiguration(
        smartspacerId: String,
        timeoutEnabled: Boolean? = null,
        timeout: Timeout? = null
    ) {
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onChanged
        ) {
            ComplicationData(
                timeoutEnabled ?: it?.timeoutEnabled ?: true,
                timeout ?: it?.timeout ?: Timeout.SIXTY_MINUTES
            )
        }
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        SmartspacerComplicationProvider.notifyChange(
            context, SleepComplication::class.java, smartspacerId
        )
    }

}