package com.kieronquinn.app.smartspacer.plugin.aftership.ui.screens.configuration

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.AftershipRepository
import com.kieronquinn.app.smartspacer.plugin.aftership.targets.AftershipTarget
import com.kieronquinn.app.smartspacer.plugin.aftership.targets.AftershipTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class AftershipTargetConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(smartspacerId: String)
    abstract fun onShowImageChanged(enabled: Boolean)
    abstract fun onShowMapChanged(enabled: Boolean)
    abstract fun onEnableUpdatesChanged(enabled: Boolean)
    abstract fun onResetClearedClicked()

    sealed class State {
        object Loading: State()
        data class Loaded(val targetData: TargetData): State()
    }

}

class AftershipTargetConfigurationViewModelImpl(
    private val dataRepository: DataRepository,
    private val aftershipRepository: AftershipRepository
): AftershipTargetConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val targetData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getTargetDataFlow(it, TargetData::class.java)
    }

    override val state = targetData.mapLatest {
        State.Loaded(it ?: TargetData())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(smartspacerId: String) {
        viewModelScope.launch {
            this@AftershipTargetConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onShowImageChanged(enabled: Boolean) {
        updateTargetData {
            copy(showImage = enabled)
        }
    }

    override fun onShowMapChanged(enabled: Boolean) {
        updateTargetData {
            copy(showMap = enabled)
        }
    }

    override fun onEnableUpdatesChanged(enabled: Boolean) {
        updateTargetData {
            copy(enableUpdates = enabled)
        }
    }

    override fun onResetClearedClicked() {
        aftershipRepository.clearDismissedPackages()
    }

    private fun updateTargetData(block: TargetData.() -> TargetData) {
        val id = smartspacerId.value ?: return
        dataRepository.updateTargetData(
            id,
            TargetData::class.java,
            TargetData.TYPE_AFTERSHIP,
            ::onDataChanged
        ) {
            val data = it ?: TargetData()
            block(data)
        }
    }

    private fun onDataChanged(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(context, AftershipTarget::class.java, smartspacerId)
    }

}