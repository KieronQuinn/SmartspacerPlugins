package com.kieronquinn.app.smartspacer.plugin.googlefinance.ui.screens.configuration

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlefinance.targets.GoogleFinanceTarget
import com.kieronquinn.app.smartspacer.plugin.googlefinance.targets.GoogleFinanceTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.googlefinance.targets.GoogleFinanceTarget.TargetData.MinimumTrendDirection
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
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
    abstract fun onReconfigureClicked(
        context: Context,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    )
    abstract fun onMinimumTrendClicked()
    abstract fun onMinimumTrendChanged(trend: Double?)
    abstract fun onMinimumTrendDirectionChanged(direction: MinimumTrendDirection)
    abstract fun onFilterExpandedChanged(enabled: Boolean)
    abstract fun onClearDismissedClicked()

    sealed class State {
        data object Loading: State()
        data class Loaded(val targetData: TargetData): State()
    }

}

class ConfigurationViewModelImpl(
    private val dataRepository: DataRepository,
    private val navigation: ContainerNavigation
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

    override fun onReconfigureClicked(
        context: Context,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val smartspacerId = smartspacerId.value ?: return
        val intentSender = SmartspacerWidgetProvider.getReconfigureIntentSender(
            context, smartspacerId
        ) ?: return
        launcher.launch(IntentSenderRequest.Builder(intentSender).build())
    }

    override fun onMinimumTrendClicked() {
        val current = (state.value as? State.Loaded)?.targetData?.minimumTrend?.toString() ?: ""
        viewModelScope.launch {
            navigation.navigate(ConfigurationFragmentDirections.actionConfigurationFragmentToMinimumTrendFragment(
                current
            ))
        }
    }

    override fun onMinimumTrendChanged(trend: Double?) {
        updateTargetData {
            copy(minimumTrend = trend)
        }
    }

    override fun onMinimumTrendDirectionChanged(direction: MinimumTrendDirection) {
        updateTargetData {
            copy(minimumTrendDirection = direction)
        }
    }

    override fun onFilterExpandedChanged(enabled: Boolean) {
        updateTargetData {
            copy(filterExpanded = enabled)
        }
    }

    override fun onClearDismissedClicked() {
        updateTargetData {
            copy(dismissedAt = null)
        }
    }

    private fun updateTargetData(block: TargetData.() -> TargetData) {
        val smartspacerId = smartspacerId.value ?: return
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE,
            ::onUpdated
        ) {
            val data = it ?: TargetData()
            block(data)
        }
    }

    private fun onUpdated(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(
            context, GoogleFinanceTarget::class.java, smartspacerId
        )
    }

}