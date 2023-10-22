package com.kieronquinn.app.smartspacer.plugins.datausage.ui.screens.configuration

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugins.datausage.complications.DataUsageComplication
import com.kieronquinn.app.smartspacer.plugins.datausage.complications.DataUsageComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugins.datausage.complications.DataUsageComplication.ComplicationData.Network
import com.kieronquinn.app.smartspacer.plugins.datausage.utils.extensions.hasDataUsagePermission
import com.kieronquinn.app.smartspacer.plugins.datausage.utils.extensions.hasEthernet
import com.kieronquinn.app.smartspacer.plugins.datausage.utils.extensions.hasMobileData
import com.kieronquinn.app.smartspacer.plugins.datausage.utils.extensions.hasWiFi
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class ConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun onResume()
    abstract fun setup(smartspacerId: String)
    abstract fun onPermissionClicked()
    abstract fun onNetworkChanged(network: Network)
    abstract fun onCycleDayChanged(day: Int)
    abstract fun onRefreshRateChanged(refreshRate: ComplicationData.RefreshRate)

    sealed class State {
        object Loading: State()
        data class Loaded(
            val networkStatus: NetworkStatus?,
            val data: ComplicationData
        ): State()
    }

    data class NetworkStatus(
        val hasMobileData: Boolean,
        val hasWiFi: Boolean,
        val hasEthernet: Boolean
    ) {

        fun getValidNetworks(): List<Network> {
            return Network.values().filter {
                when(it) {
                    Network.MOBILE_DATA -> hasMobileData
                    Network.WIFI -> hasWiFi
                    Network.ETHERNET -> hasEthernet
                }
            }
        }

    }

}

class ConfigurationViewModelImpl(
    context: Context,
    private val navigation: ContainerNavigation,
    private val dataRepository: DataRepository
): ConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)
    private val resumeBus = MutableStateFlow(System.currentTimeMillis())

    private val complicationData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getComplicationDataFlow(it, ComplicationData::class.java)
    }

    private val hasPermission = resumeBus.mapLatest {
        context.hasDataUsagePermission()
    }

    private val networkStatus = hasPermission.filter { it }.mapLatest {
        NetworkStatus(
            context.hasMobileData(),
            context.hasWiFi(),
            context.hasEthernet()
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    override val state = combine(
        networkStatus,
        complicationData
    ) { networkStatus, data ->
        State.Loaded( networkStatus, data ?: ComplicationData())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onResume() {
        viewModelScope.launch {
            resumeBus.emit(System.currentTimeMillis())
        }
    }

    override fun setup(smartspacerId: String) {
        viewModelScope.launch {
            this@ConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onPermissionClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun onCycleDayChanged(day: Int) {
        updateComplicationData {
            copy(cycleDay = day)
        }
    }

    override fun onNetworkChanged(network: Network) {
        updateComplicationData {
            copy(network = network)
        }
    }

    override fun onRefreshRateChanged(refreshRate: ComplicationData.RefreshRate) {
        updateComplicationData {
            copy(refreshRate = refreshRate)
        }
    }

    private fun updateComplicationData(block: ComplicationData.() -> ComplicationData) {
        val smartspacerId = smartspacerId.value ?: return
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onChanged
        ) {
            block(it ?: ComplicationData())
        }
    }

    private fun onChanged(context: Context, smartspacerId: String){
        SmartspacerComplicationProvider.notifyChange(
            context, DataUsageComplication::class.java, smartspacerId
        )
    }

}