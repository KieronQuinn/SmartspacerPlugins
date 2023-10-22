package com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.apikey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import kotlinx.coroutines.launch

abstract class ApiKeyViewModel: ViewModel() {

    abstract val input: String

    abstract fun setInitialInput(input: String)
    abstract fun setInput(input: String)
    abstract fun dismiss()

}

class ApiKeyViewModelImpl(
    private val navigation: ContainerNavigation
): ApiKeyViewModel() {

    private var customInput: String? = null
    private var _input: String? = null

    override val input: String
        get() = customInput ?: _input ?: ""

    override fun setInitialInput(input: String) {
        _input = input
    }

    override fun setInput(input: String) {
        customInput = input
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

}