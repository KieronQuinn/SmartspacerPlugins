package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.popup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository.Valuable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class PopupWalletDialogViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setupWithId(valuableId: String)

    sealed class State {
        object Loading: State()
        object Error: State()
        data class Loaded(val valuable: Valuable): State()
    }

}

class PopupWalletDialogViewModelImpl(
    walletRepository: GoogleWalletRepository
): PopupWalletDialogViewModel() {

    private val valuableId = MutableStateFlow<String?>(null)

    override val state = valuableId.filterNotNull().mapLatest {
        walletRepository.getValuableById(it)?.let { card -> State.Loaded(card) } ?: State.Error
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setupWithId(valuableId: String) {
        viewModelScope.launch {
            this@PopupWalletDialogViewModelImpl.valuableId.emit(valuableId)
        }
    }

}