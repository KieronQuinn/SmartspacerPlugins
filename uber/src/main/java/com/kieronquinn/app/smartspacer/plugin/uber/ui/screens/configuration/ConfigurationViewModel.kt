package com.kieronquinn.app.smartspacer.plugin.uber.ui.screens.configuration

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.uber.targets.UberTarget
import com.kieronquinn.app.smartspacer.plugin.uber.targets.UberTarget.UberTargetData
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class ConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(smartspacerId: String)
    abstract fun onShowExpandedInfoChanged(enabled: Boolean)
    abstract fun onHideNotificationChanged(enabled: Boolean)

    sealed class State {
        object Loading: State()
        data class Loaded(val targetData: UberTargetData): State()
    }

}

class ConfigurationViewModelImpl(
    private val dataRepository: DataRepository
): ConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val targetData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getTargetDataFlow(it, UberTargetData::class.java)
    }

    override val state = targetData.mapLatest {
        State.Loaded(it ?: UberTargetData())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(smartspacerId: String) {
        viewModelScope.launch {
            this@ConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onShowExpandedInfoChanged(enabled: Boolean) {
        updateTarget {
            copy(showExpandedInfo = enabled)
        }
    }

    override fun onHideNotificationChanged(enabled: Boolean) {
        updateTarget {
            copy(hideNotification = enabled)
        }
    }

    private fun updateTarget(block: UberTargetData.() -> UberTargetData) {
        val smartspacerId = smartspacerId.value ?: return
        viewModelScope.launch {
            dataRepository.updateTargetData(
                smartspacerId,
                UberTargetData::class.java,
                UberTargetData.TYPE_UBER,
                ::onChanged
            ) {
                val data = it ?: UberTargetData()
                block(data)
            }
        }
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(context, UberTarget::class.java, smartspacerId)
    }

}