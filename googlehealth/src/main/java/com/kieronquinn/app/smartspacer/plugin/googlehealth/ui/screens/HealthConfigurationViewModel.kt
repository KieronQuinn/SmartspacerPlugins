package com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlehealth.complications.GoogleHealthComplication
import com.kieronquinn.app.smartspacer.plugin.googlehealth.complications.GoogleHealthComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.googlehealth.complications.GoogleHealthComplication.ComplicationData.TapAction
import com.kieronquinn.app.smartspacer.plugin.googlehealth.complications.GoogleHealthComplication.ComplicationData.Timeout
import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.HealthMetric
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.GoogleHealthRepository
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class HealthConfigurationViewModel : ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setupWithId(smartspacerId: String)
    abstract fun onMetricClicked()
    abstract fun onMetricChanged(metric: HealthMetric)
    abstract fun onTapActionChanged(action: TapAction)
    abstract fun onTimeoutEnabledChanged(enabled: Boolean)
    abstract fun onTimeoutChanged(timeout: Timeout)

    sealed class State {
        object Loading : State()
        data class Loaded(
            val selected: HealthMetric?,
            val timeoutEnabled: Boolean,
            val timeout: Timeout,
            val action: TapAction,
            val metricAvailable: Boolean
        ) : State()
    }

}

class HealthConfigurationViewModelImpl(
    private val dataRepository: DataRepository,
    private val navigation: ContainerNavigation,
    googleHealthRepository: GoogleHealthRepository
) : HealthConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val complicationData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getComplicationDataFlow(it, ComplicationData::class.java).map { data ->
            data ?: ComplicationData()
        }
    }

    private val metricAvailable = complicationData.flatMapLatest {
        val metric = it.metric ?: return@flatMapLatest flowOf(true)
        googleHealthRepository.isHealthMetricAvailable(metric)
    }

    override val state = combine(
        complicationData,
        metricAvailable
    ) { complicationData, available ->
        State.Loaded(
            complicationData.metric,
            complicationData.timeoutEnabled,
            complicationData.timeout,
            complicationData.action,
            available
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setupWithId(smartspacerId: String) {
        viewModelScope.launch {
            this@HealthConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onMetricClicked() {
        viewModelScope.launch {
            navigation.navigate(HealthConfigurationFragmentDirections
                .actionHealthConfigurationFragmentToHealthConfigurationFocusFragment())
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

    override fun onMetricChanged(metric: HealthMetric) {
        val smartspacerId = smartspacerId.value ?: return
        updateConfiguration(smartspacerId, metric = metric)
    }

    override fun onTapActionChanged(action: TapAction) {
        val smartspacerId = smartspacerId.value ?: return
        updateConfiguration(smartspacerId, action = action)
    }

    private fun updateConfiguration(
        smartspacerId: String,
        metric: HealthMetric? = null,
        action: TapAction? = null,
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
                metric ?: it?.metric,
                timeoutEnabled ?: it?.timeoutEnabled ?: false,
                timeout ?: it?.timeout ?: Timeout.SIXTY_MINUTES,
                action ?: it?.action ?: TapAction.OPEN_METRIC
            )
        }
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        SmartspacerComplicationProvider.notifyChange(
            context, GoogleHealthComplication::class.java, smartspacerId
        )
    }

}