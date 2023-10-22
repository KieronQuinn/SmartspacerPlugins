package com.kieronquinn.app.smartspacer.plugin.googlewallet.ui.screens.configuration.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.EncryptedSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleApiRepository
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

abstract class SignInWithGoogleViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setCookie(cookie: String)
    abstract fun close()

    sealed class State {
        object SignIn: State()
        object Success: State()
        object Error: State()
    }

}

class SignInWithGoogleViewModelImpl(
    private val navigation: ContainerNavigation,
    encryptedSettings: EncryptedSettingsRepository,
    googleApiRepository: GoogleApiRepository
): SignInWithGoogleViewModel() {

    private val cookie = MutableStateFlow<String?>(null)

    private val _state = cookie.mapLatest {
        if(it == null) return@mapLatest State.SignIn
        val aasToken = googleApiRepository.getAasToken(it)
        if(aasToken != null){
            encryptedSettings.aasToken.set(aasToken)
            State.Success
        }else{
            State.Error
        }
    }

    override val state = MutableStateFlow<State>(State.SignIn)

    override fun setCookie(cookie: String) {
        viewModelScope.launch {
            this@SignInWithGoogleViewModelImpl.cookie.emit(cookie)
        }
    }

    override fun close() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    private fun setupState() = viewModelScope.launch {
        _state.collect {
            state.emit(it)
        }
    }

    init {
        setupState()
    }

}