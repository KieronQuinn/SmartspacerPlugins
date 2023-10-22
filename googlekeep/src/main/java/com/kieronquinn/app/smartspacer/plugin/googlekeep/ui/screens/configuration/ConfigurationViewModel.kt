package com.kieronquinn.app.smartspacer.plugin.googlekeep.ui.screens.configuration

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlekeep.targets.GoogleKeepTarget
import com.kieronquinn.app.smartspacer.plugin.googlekeep.targets.GoogleKeepTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
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
    abstract fun onSelectNoteClicked(
        context: Context,
        intentSender: ActivityResultLauncher<IntentSenderRequest>
    )
    abstract fun onShowIndentedChanged(enabled: Boolean)
    abstract fun onHideIfEmptyChanged(enabled: Boolean)

    sealed class State {
        object Loading: State()
        data class Loaded(val data: TargetData): State()
    }

}

class ConfigurationViewModelImpl(
    private val dataRepository: DataRepository
): ConfigurationViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val targetData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getTargetDataFlow(it, TargetData::class.java)
    }

    override val state = targetData.mapLatest {
        State.Loaded(it ?: TargetData())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(smartspacerId: String) {
        viewModelScope.launch {
            this@ConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onSelectNoteClicked(
        context: Context,
        intentSender: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val smartspacerId = smartspacerId.value ?: return
        SmartspacerWidgetProvider.getReconfigureIntentSender(context, smartspacerId)?.let {
            intentSender.launch(IntentSenderRequest.Builder(it).build())
        }
    }

    override fun onShowIndentedChanged(enabled: Boolean) {
        updateTargetData {
            copy(showIndented = enabled)
        }
    }

    override fun onHideIfEmptyChanged(enabled: Boolean) {
        updateTargetData {
            copy(hideIfEmpty = enabled)
        }
    }

    private fun updateTargetData(block: TargetData.() -> TargetData) {
        val smartspacerId = smartspacerId.value ?: return
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE,
            ::onChanged
        ) {
            val current = it ?: TargetData()
            block(current)
        }
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(context, GoogleKeepTarget::class.java, smartspacerId)
    }

}