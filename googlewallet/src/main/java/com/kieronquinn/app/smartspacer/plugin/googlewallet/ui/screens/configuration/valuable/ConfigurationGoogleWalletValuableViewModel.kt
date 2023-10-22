package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.targets.GoogleWalletValuableTarget
import com.kieronquinn.app.smartspacer.plugin.googlewallet.targets.GoogleWalletValuableTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class ConfigurationGoogleWalletValuableViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setupWithId(smartspacerId: String)

    abstract fun onSelectCardClicked()
    abstract fun onShowImageChanged(enabled: Boolean)
    abstract fun onShowPopupChanged(enabled: Boolean)

    sealed class State {
        object Loading: State()
        data class Loaded(
            val settings: TargetData?,
            val cardName: String?,
            val cardHasImage: Boolean
        ): State()
    }

}

class ConfigurationGoogleWalletValuableViewModelImpl(
    private val navigation: ContainerNavigation,
    private val dataRepository: DataRepository,
    googleWalletRepository: GoogleWalletRepository
): ConfigurationGoogleWalletValuableViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val settings = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getTargetDataFlow(it, TargetData::class.java).map { data ->
            data ?: TargetData()
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val selectedCard = settings.mapLatest {
        googleWalletRepository.getValuableById(it?.valuableId ?: return@mapLatest null)
    }

    override val state = combine(
        settings.filterNotNull(),
        selectedCard
    ) { data, card ->
        State.Loaded(
            data, card?.getGroupingInfo()?.groupingTitle, card?.cardImage != null
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setupWithId(smartspacerId: String) {
        viewModelScope.launch {
            this@ConfigurationGoogleWalletValuableViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onShowImageChanged(enabled: Boolean) {
        val id = smartspacerId.value ?: return
        dataRepository.updateTargetData(
            id,
            TargetData::class.java,
            TargetData.TYPE,
            ::onTargetDataSaved
        ) {
            (it ?: TargetData()).copy(showCardImage = enabled)
        }
    }

    override fun onShowPopupChanged(enabled: Boolean) {
        val id = smartspacerId.value ?: return
        dataRepository.updateTargetData(
            id,
            TargetData::class.java,
            TargetData.TYPE,
            ::onTargetDataSaved
        ) {
            (it ?: TargetData()).copy(showAsPopup = enabled)
        }
    }

    private fun onTargetDataSaved(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(
            context, GoogleWalletValuableTarget::class.java, smartspacerId
        )
    }

    override fun onSelectCardClicked() {
        viewModelScope.launch {
            navigation.navigate(ConfigurationGoogleWalletValuableFragmentDirections
                .actionConfigurationGoogleWalletValuableFragmentToConfigurationGoogleWalletValuablePickerFragment())
        }
    }

}