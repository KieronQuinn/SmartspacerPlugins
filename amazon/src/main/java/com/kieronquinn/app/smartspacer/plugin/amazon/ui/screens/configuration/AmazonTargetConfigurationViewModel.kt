package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.configuration

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.model.AmazonDomain
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Delivery
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDelivery.Status
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.invert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class AmazonTargetConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun onSignInClicked()
    abstract fun onShowAdvancedClicked()
    abstract fun onShowProductImageChanged(enabled: Boolean)
    abstract fun onReloadClicked()
    abstract fun onDomainChanged(domain: AmazonDomain)
    abstract fun onClearDismissedClicked()
    abstract fun onKnownOrdersClicked()
    abstract fun onTrackableOrdersClicked()
    abstract fun signOut()

    sealed class State {
        object Loading: State()
        data class SignIn(val url: String): State()
        data class SignedIn(
            val showProductImage: Boolean,
            val showAdvanced: Boolean,
            val domain: AmazonDomain?,
            val deliveries: List<Delivery>
        ): State()
        object Unsupported: State()
    }

}

class AmazonTargetConfigurationViewModelImpl(
    private val navigation: ContainerNavigation,
    private val amazonRepository: AmazonRepository,
    settings: AmazonSettingsRepository
): AmazonTargetConfigurationViewModel() {

    private val syncState = MutableStateFlow<SyncState?>(null)
    private val showProductImage = settings.showProductImage
    private val showAdvanced = settings.showAdvanced
    private val domain = settings.domain

    override val state = combine(
        amazonRepository.getDomain(),
        syncState.filterNotNull(),
        showProductImage.asFlow(),
        showAdvanced.asFlow(),
    ){ domain, state, product, advanced ->
        when(state) {
            SyncState.FAILED -> {
                val url = domain?.let {
                    amazonRepository.getOrdersUrl(it)
                }
                if(url != null){
                    State.SignIn(url)
                }else State.Unsupported
            }
            SyncState.SUCCESS -> State.SignedIn(
                product,
                advanced,
                domain,
                amazonRepository.getDeliveries()
            )
            SyncState.LOADING -> State.Loading
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onSignInClicked() {
        val url = (state.value as? State.SignIn)?.url ?: return
        viewModelScope.launch {
            navigation.navigate(AmazonTargetConfigurationFragmentDirections
                .actionAmazonTargetConfigurationFragmentToAmazonTargetConfigurationSignInFragment(url))
        }
    }

    override fun onShowProductImageChanged(enabled: Boolean) {
        viewModelScope.launch {
            showProductImage.set(enabled)
            amazonRepository.reloadTarget()
        }
    }

    override fun onReloadClicked() {
        syncDeliveries(reloadList = true)
    }

    override fun onShowAdvancedClicked() {
        viewModelScope.launch {
            showAdvanced.invert()
        }
    }

    override fun onDomainChanged(domain: AmazonDomain) {
        viewModelScope.launch {
            val current = this@AmazonTargetConfigurationViewModelImpl.domain.get()
            if(current == domain) return@launch
            this@AmazonTargetConfigurationViewModelImpl.domain.set(domain)
            signOut()
        }
    }

    override fun onClearDismissedClicked() {
        viewModelScope.launch {
            amazonRepository.clearDismissedDeliveries()
        }
    }

    override fun onKnownOrdersClicked() {
        val deliveries = (state.value as? State.SignedIn)?.deliveries ?: return
        viewModelScope.launch {
            navigation.navigate(
                AmazonTargetConfigurationFragmentDirections
                    .actionAmazonTargetConfigurationFragmentToAmazonTargetConfigurationDumpFragment(
                        deliveries.toTypedArray(),
                        R.string.target_amazon_settings_known_orders_title
                    )
            )
        }
    }

    override fun onTrackableOrdersClicked() {
        val deliveries = (state.value as? State.SignedIn)?.deliveries?.filter {
            it.getBestStatus() != Status.DELIVERED && it.trackingData != null
        } ?: return
        viewModelScope.launch {
            navigation.navigate(
                AmazonTargetConfigurationFragmentDirections
                    .actionAmazonTargetConfigurationFragmentToAmazonTargetConfigurationDumpFragment(
                        deliveries.toTypedArray(),
                        R.string.target_amazon_settings_trackable_orders_title
                    )
            )
        }
    }

    private fun syncDeliveries(
        reloadList: Boolean = false,
        reloadListIfEmpty: Boolean = false
    ) = viewModelScope.launch {
        if(syncState.value == SyncState.LOADING) return@launch //Already loading
        syncState.emit(SyncState.LOADING)
        if(amazonRepository.syncDeliveriesNow(reloadList, reloadListIfEmpty)){
            syncState.emit(SyncState.SUCCESS)
        }else{
            syncState.emit(SyncState.FAILED)
        }
    }

    override fun signOut() {
        viewModelScope.launch {
            amazonRepository.clearState()
            syncState.emit(SyncState.FAILED)
        }
    }

    init {
        syncDeliveries(reloadListIfEmpty = true)
    }

    enum class SyncState {
        LOADING, SUCCESS, FAILED
    }

}