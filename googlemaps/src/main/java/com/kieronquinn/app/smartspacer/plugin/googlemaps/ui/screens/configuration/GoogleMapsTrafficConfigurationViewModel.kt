package com.kieronquinn.app.smartspacer.plugin.googlemaps.ui.screens.configuration

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository.TrafficLevel
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository.ZoomMode
import com.kieronquinn.app.smartspacer.plugin.googlemaps.targets.GoogleMapsTrafficTarget
import com.kieronquinn.app.smartspacer.plugin.googlemaps.targets.GoogleMapsTrafficTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class GoogleMapsTrafficConfigurationViewModel(): ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setupWithId(id: String)
    abstract fun onZoomModeChanged(zoomMode: ZoomMode)
    abstract fun onTrafficLevelChanged(trafficLevel: TrafficLevel)

    sealed class State {
        object Loading: State()
        data class Loaded(
            val zoomMode: ZoomMode,
            val minTrafficLevel: TrafficLevel
        ): State()
    }

}

class GoogleMapsTrafficConfigurationViewModelImpl(
    private val dataRepository: DataRepository
): GoogleMapsTrafficConfigurationViewModel() {

    private val id = MutableStateFlow<String?>(null)

    private val settings = id.filterNotNull().flatMapLatest { id ->
        dataRepository.getTargetDataFlow(id, TargetData::class.java).map {
            it ?: TargetData()
        }
    }.flowOn(Dispatchers.IO)

    override val state = settings.mapLatest {
        State.Loaded(it.mode, it.minTrafficLevel)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setupWithId(id: String) {
        viewModelScope.launch {
            this@GoogleMapsTrafficConfigurationViewModelImpl.id.emit(id)
        }
    }

    override fun onTrafficLevelChanged(trafficLevel: TrafficLevel) {
        updateData(trafficLevel = trafficLevel)
    }

    override fun onZoomModeChanged(zoomMode: ZoomMode) {
        updateData(zoomMode = zoomMode)
    }

    private fun updateData(
        trafficLevel: TrafficLevel? = null,
        zoomMode: ZoomMode? = null
    ) {
        val id = id.value ?: return
        dataRepository.updateTargetData(
            id,
            TargetData::class.java,
            TargetData.TYPE,
            ::notifyChange
        ) {
            (it ?: TargetData()).let { target ->
                target.copy(
                    mode = zoomMode ?: target.mode,
                    minTrafficLevel = trafficLevel ?: target.minTrafficLevel
                )
            }
        }
    }

    private fun notifyChange(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(
            context, GoogleMapsTrafficTarget::class.java, smartspacerId
        )
    }

}