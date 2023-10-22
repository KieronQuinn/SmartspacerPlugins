package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.dynamic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.EncryptedSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.SyncValuablesResult
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class ConfigurationGoogleWalletDynamicViewModel: ViewModel() {

    abstract val state: StateFlow<State>
    abstract val reloadResultBus: Flow<Boolean>

    abstract fun setupWithId(smartspacerId: String)
    abstract fun onSignInClicked()
    abstract fun onAllowMeteredConnectionsChanged(enabled: Boolean)
    abstract fun onReloadClicked()

    sealed class State {
        object Loading: State()
        object SignInRequired: State()
        object Error: State()
        data class Loaded(val isReloading: Boolean, val allowOnMetered: Boolean): State()
    }

}

class ConfigurationGoogleWalletDynamicViewModelImpl(
    private val navigation: ContainerNavigation,
    private val googleWalletRepository: GoogleWalletRepository,
    private val encryptedSettings: EncryptedSettingsRepository
): ConfigurationGoogleWalletDynamicViewModel() {

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

    private val isReloading = MutableStateFlow(false)

    override val state = combine(
        syncValuablesState,
        isReloading,
        encryptedSettings.reloadOnMeteredConnection.asFlow()
    ) { state, reloading, allowOnMetered ->
        when(state){
            SyncValuablesResult.SUCCESS -> State.Loaded(reloading, allowOnMetered)
            SyncValuablesResult.ERROR -> State.Error
            SyncValuablesResult.FATAL_ERROR -> State.SignInRequired
            null -> State.Loading
        }
    }.flowOn(Dispatchers.IO).stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override val reloadResultBus = MutableSharedFlow<Boolean>()

    override fun setupWithId(smartspacerId: String) {
        viewModelScope.launch {
            this@ConfigurationGoogleWalletDynamicViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onSignInClicked() {
        viewModelScope.launch {
            navigation.navigate(ConfigurationGoogleWalletDynamicFragmentDirections
                .actionConfigurationGoogleWalletDynamicFragmentToSignInWithGoogleFragment2())
        }
    }

    override fun onAllowMeteredConnectionsChanged(enabled: Boolean) {
        viewModelScope.launch {
            encryptedSettings.reloadOnMeteredConnection.set(enabled)
        }
    }

    override fun onReloadClicked() {
        viewModelScope.launch {
            isReloading.emit(true)
            val result = googleWalletRepository.syncValuables()
            reloadResultBus.emit(result == SyncValuablesResult.SUCCESS)
            isReloading.emit(false)
        }
    }

}