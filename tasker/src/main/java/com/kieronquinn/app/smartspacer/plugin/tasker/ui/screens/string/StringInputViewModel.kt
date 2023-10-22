package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string

import android.webkit.URLUtil
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Manipulative.Companion.containsTaskerVariable
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.InputValidation
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.isVariable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class StringInputViewModel: ViewModel() {

    abstract val error: StateFlow<Int?>
    abstract val input: String

    abstract fun setInitialInput(input: String)
    abstract fun setInput(input: String)
    abstract fun validate(validation: InputValidation?): Boolean
    abstract fun dismiss()

}

class StringInputViewModelImpl(
    private val navigation: ContainerNavigation
): StringInputViewModel() {

    companion object {
        private val REGEX_ASPECT_RATIO_VARIABLES = "%[A-Za-z0-9]+:%[A-Za-z0-9]+".toRegex()
        private val REGEX_ASPECT_RATIO = "[0-9]+:[0-9]+".toRegex()
    }

    private var customInput: String? = null
    private var _input: String? = null

    override val error = MutableStateFlow<Int?>(null)

    override val input: String
        get() = customInput ?: _input ?: ""

    override fun setInitialInput(input: String) {
        _input = input
    }

    override fun setInput(input: String) {
        customInput = input
        viewModelScope.launch {
            error.emit(null)
        }
    }

    override fun validate(validation: InputValidation?): Boolean {
        return when(validation) {
            null -> true
            InputValidation.NOT_EMPTY -> input.isNotBlank()
            InputValidation.URL -> {
                //If the input contains a variable, assume they know what they're doing and allow it
                input.containsTaskerVariable() || URLUtil.isValidUrl(input)
            }
            InputValidation.ASPECT_RATIO -> input.isValidAspectRatio()
            InputValidation.FRAME_DURATION -> input.isValidFrameDuration()
            InputValidation.WIDTH,
            InputValidation.HEIGHT -> input.isValidWidthOrHeight()
            InputValidation.TIME -> input.isValidEpochTimestamp()
            InputValidation.TASKER_VARIABLE -> input.isVariable()
            InputValidation.TEMPERATURE -> input.isValidTemperature()
            InputValidation.REFRESH_PERIOD -> input.isValidRefreshPeriod()
            InputValidation.TAP_ACTION_ID -> input.isNotBlank()
        }.also {
            if(!it) {
                viewModelScope.launch {
                    error.emit(validation?.error ?: return@launch)
                }
            }
        }
    }

    private fun String.isValidFrameDuration(): Boolean {
        if(input.isVariable()) return true
        val asInt = toIntOrNull() ?: return false
        return asInt > 0
    }

    private fun String.isValidWidthOrHeight(): Boolean {
        if(input.isEmpty()) return true
        if(input.isVariable()) return true
        val asInt = toIntOrNull() ?: return false
        return asInt > 0
    }

    private fun String.isValidEpochTimestamp(): Boolean {
        return isBlank() || isVariable() || toLongOrNull()?.let { it >= 0 } ?: false
    }

    private fun String.isValidTemperature(): Boolean {
        return isVariable() || toIntOrNull() != null
    }

    private fun String.isValidAspectRatio(): Boolean {
        return isEmpty() || REGEX_ASPECT_RATIO.matches(this)
                || REGEX_ASPECT_RATIO_VARIABLES.matches(this)
    }

    private fun String.isValidRefreshPeriod(): Boolean {
        return isVariable() || toIntOrNull()?.let { it > 0 } ?: false
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

}