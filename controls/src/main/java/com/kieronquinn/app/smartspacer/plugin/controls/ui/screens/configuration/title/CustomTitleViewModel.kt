package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.title

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import kotlinx.coroutines.launch

abstract class CustomTitleViewModel: ViewModel() {

    abstract val input: String

    abstract fun setInitialInput(input: String)
    abstract fun setInput(input: String)
    abstract fun dismiss()

}

class CustomTitleViewModelImpl(
    private val navigation: ContainerNavigation
): CustomTitleViewModel() {

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