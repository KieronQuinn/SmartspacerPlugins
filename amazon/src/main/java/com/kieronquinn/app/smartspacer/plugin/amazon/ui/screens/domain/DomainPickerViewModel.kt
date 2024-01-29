package com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.domain

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.AmazonDomain
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class DomainPickerViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun onDomainClicked(domain: AmazonDomain, isSetup: Boolean)

    sealed class State {
        data object Loading: State()
        data class Loaded(
            val recommendedDomain: AmazonDomain?,
            val otherDomains: List<AmazonDomain>
        ): State()
    }

}

class DomainPickerViewModelImpl(
    private val navigation: ContainerNavigation,
    private val amazonRepository: AmazonRepository,
    context: Context,
    settingsRepository: AmazonSettingsRepository,
): DomainPickerViewModel() {

    private val domain = settingsRepository.domain

    private val marketplaceDomain = flow {
        emit(amazonRepository.getAmazonMarketplaceDomain())
    }.flowOn(Dispatchers.IO)

    private val allDomains = flow {
        emit(AmazonDomain.entries)
    }

    override val state = combine(
        marketplaceDomain,
        allDomains
    ) { recommended, all ->
        val otherDomains = all.filterNot { it == recommended }
            .sortedBy { context.getString(it.countryRes).lowercase() }
        State.Loaded(recommended, otherDomains)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun onDomainClicked(domain: AmazonDomain, isSetup: Boolean) {
        viewModelScope.launch {
            this@DomainPickerViewModelImpl.domain.set(domain)
            amazonRepository.signOut()
            if(isSetup) {
                navigation.navigate(
                    DomainPickerFragmentDirections.actionDomainPickerFragmentToInfoFragment()
                )
            }else{
                navigation.navigateBack()
            }
        }
    }

}