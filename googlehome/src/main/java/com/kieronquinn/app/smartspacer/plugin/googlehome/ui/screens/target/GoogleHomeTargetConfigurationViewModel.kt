package com.kieronquinn.app.smartspacer.plugin.googlehome.ui.screens.target

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlehome.model.HideMode
import com.kieronquinn.app.smartspacer.plugin.googlehome.targets.GoogleHomeTarget
import com.kieronquinn.app.smartspacer.plugin.googlehome.targets.GoogleHomeTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class GoogleHomeTargetConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract fun setupWithId(id: String)
    abstract fun onHideModeChanged(hideMode: HideMode)

    sealed class State {
        data object Loading: State()
        data class Loaded(val hideMode: HideMode): State()
    }

}

class GoogleHomeTargetConfigurationViewModelImpl(
    private val dataRepository: DataRepository
): GoogleHomeTargetConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val targetData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getTargetDataFlow(it, TargetData::class.java).map { data ->
            data ?: TargetData()
        }
    }

    override val state = targetData.mapLatest {
        State.Loaded(it.hideMode)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setupWithId(id: String) {
        viewModelScope.launch {
            smartspacerId.emit(id)
        }
    }

    override fun onHideModeChanged(hideMode: HideMode) {
        val smartspacerId = smartspacerId.value ?: return
        updateConfiguration(smartspacerId, hideMode)
    }

    private fun updateConfiguration(
        smartspacerId: String,
        hideMode: HideMode
    ) {
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE,
            ::onChanged
        ) {
            TargetData(hideMode = hideMode)
        }
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(
            context, GoogleHomeTarget::class.java, smartspacerId
        )
    }

}