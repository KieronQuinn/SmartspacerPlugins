package com.kieronquinn.app.smartspacer.plugins.yahoosport.ui.screens.configuration

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.allowBackground
import com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories.GameRepository
import com.kieronquinn.app.smartspacer.plugins.yahoosport.targets.YahooSportTarget
import com.kieronquinn.app.smartspacer.plugins.yahoosport.targets.YahooSportTarget.TargetData
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
    abstract fun onClearDismissedClicked()
    abstract fun onTeamChanged()

    sealed class State {
        object Loading: State()
        data class Loaded(val targetData: TargetData): State()
    }

}

class ConfigurationViewModelImpl(
    private val dataRepository: DataRepository,
    private val gameRepository: GameRepository
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

    @SuppressLint("NewApi")
    override fun onReconfigureClicked(
        context: Context,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        val smartspacerId = smartspacerId.value ?: return
        val reconfigureIntentSender = SmartspacerWidgetProvider.getReconfigureIntentSender(
            context, smartspacerId
        ) ?: return
        launcher.launch(
            IntentSenderRequest.Builder(reconfigureIntentSender).build(),
            ActivityOptionsCompat.makeBasic().allowBackground()
        )
    }

    override fun onClearDismissedClicked() {
        val id = smartspacerId.value ?: return
        dataRepository.updateTargetData(
            id,
            TargetData::class.java,
            TargetData.TYPE,
            ::onChanged
        ) {
            val data = it ?: TargetData()
            data.copy(dismissedGames = emptySet())
        }
    }

    override fun onTeamChanged() {
        val id = smartspacerId.value ?: return
        gameRepository.setGame(id, null)
        gameRepository.setTeamName(id, null)
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(context, YahooSportTarget::class.java, smartspacerId)
    }

}