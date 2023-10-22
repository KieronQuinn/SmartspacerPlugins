package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.valuable.picker

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlewallet.R
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.EncryptedSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.SyncValuablesResult
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable
import com.kieronquinn.app.smartspacer.plugin.googlewallet.targets.GoogleWalletValuableTarget
import com.kieronquinn.app.smartspacer.plugin.googlewallet.targets.GoogleWalletValuableTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class ConfigurationGoogleWalletValuablePickerViewModel: ViewModel() {

    companion object {
        val ALLOWED_VALUABLE_TYPES = mapOf(
            Valuable.LoyaltyCard::class.java to R.string.target_wallet_valuable_type_loyalty_card,
            Valuable.GiftCard::class.java to R.string.target_wallet_valuable_type_gift_card,
            Valuable.Offer::class.java to R.string.target_wallet_valuable_type_offer,
            Valuable.HealthCard::class.java to R.string.target_wallet_valuable_type_health_card,
            Valuable.GenericCard::class.java to R.string.target_wallet_valuable_type_generic,
            Valuable.SensitiveGenericPass::class.java to R.string.target_wallet_valuable_type_sensitive_generic
        )
    }

    abstract val state: StateFlow<State>

    abstract fun setupWithId(smartspacerId: String)
    abstract fun onSignInClicked()
    abstract fun onValuableClicked(valuable: Valuable)

    sealed class State {
        object Loading: State()
        object SignInRequired: State()
        object Error: State()
        data class Loaded(val valuables: List<Valuable>): State()
    }

}

class ConfigurationGoogleWalletValuablePickerViewModelImpl(
    private val navigation: ContainerNavigation,
    private val dataRepository: DataRepository,
    googleWalletRepository: GoogleWalletRepository,
    encryptedSettings: EncryptedSettingsRepository
): ConfigurationGoogleWalletValuablePickerViewModel() {

    private val smartspacerId = MutableStateFlow<String?>(null)

    private val aasToken = encryptedSettings.aasToken.asFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val syncValuablesState = aasToken.filterNotNull().flatMapLatest { token ->
        flow {
            emit(null)
            //Prevent hitting server if there's no AAS token (user is not logged in)
            if (token.isEmpty()){
                emit(SyncValuablesResult.FATAL_ERROR)
                return@flow
            }
            googleWalletRepository.syncValuables()
            emit(SyncValuablesResult.SUCCESS)
        }
    }

    override val state = combine(
        syncValuablesState,
        googleWalletRepository.getValuables()
    ) { state, valuables ->
        when(state){
            SyncValuablesResult.SUCCESS -> State.Loaded(valuables.filterCompatible())
            SyncValuablesResult.ERROR -> State.Error
            SyncValuablesResult.FATAL_ERROR -> State.SignInRequired
            null -> State.Loading
        }
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    private fun List<Valuable>.filterCompatible(): List<Valuable> {
        return filter {
            ALLOWED_VALUABLE_TYPES.keys.contains(it::class.java)
        }
    }

    override fun setupWithId(smartspacerId: String) {
        viewModelScope.launch {
            this@ConfigurationGoogleWalletValuablePickerViewModelImpl
                .smartspacerId.emit(smartspacerId)
        }
    }

    override fun onSignInClicked() {
        viewModelScope.launch {
            navigation.navigate(ConfigurationGoogleWalletValuablePickerFragmentDirections.actionConfigurationGoogleWalletValuablePickerFragmentToSignInWithGoogleFragment())
        }
    }

    override fun onValuableClicked(valuable: Valuable) {
        val id = smartspacerId.value ?: return
        dataRepository.updateTargetData(
            id,
            TargetData::class.java,
            TargetData.TYPE,
            ::onValuableSaved
        ) {
            //Copy across the popup option if it was previously set
            val showPopup = it?.showAsPopup ?: false
            val name = valuable.getGroupingInfo()?.groupingTitle
            TargetData(valuable.id, name, showAsPopup = showPopup)
        }
    }

    private fun onValuableSaved(context: Context, smartspacerId: String) {
        SmartspacerTargetProvider.notifyChange(
            context, GoogleWalletValuableTarget::class.java, smartspacerId
        )
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

}