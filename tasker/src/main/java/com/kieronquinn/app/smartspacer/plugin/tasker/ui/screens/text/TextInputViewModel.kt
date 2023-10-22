package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text

import android.text.InputType
import android.text.TextUtils.TruncateAt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.InputValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TextInputViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(text: Text)
    abstract fun dismiss()
    abstract fun onTextClicked()
    abstract fun onTextChanged(input: String)
    abstract fun onTruncateChanged(truncateAt: TruncateAt)

    sealed class State {
        object Loading: State()
        data class Loaded(val text: Text): State()
    }

}

class TextInputViewModelImpl(
    private val navigation: ContainerNavigation
): TextInputViewModel() {

    private val initialText = MutableStateFlow<Text?>(null)
    private val text = MutableStateFlow<Text?>(null)

    override val state = combine(
        initialText.filterNotNull(),
        text
    ) { initial, current ->
        State.Loaded(current ?: initial)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(text: Text) {
        viewModelScope.launch {
            initialText.emit(text)
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onTextChanged(input: String) {
        updateText { copy(text = input) }
    }

    override fun onTruncateChanged(truncateAt: TruncateAt) {
        updateText { copy(truncateAtType = truncateAt) }
    }

    override fun onTextClicked() {
        withCurrent {
            navigation.navigate(TextInputFragmentDirections.actionTextInputFragmentToNavGraphIncludeString(
                StringInputFragment.Config(
                    text,
                    TextInputFragment.REQUEST_KEY_TEXT,
                    R.string.configuration_text_text_title,
                    R.string.configuration_text_text_content,
                    R.string.configuration_text_text_title,
                    inputValidation = InputValidation.NOT_EMPTY,
                    inputType = InputType.TYPE_CLASS_TEXT
                )
            ))
        }
    }

    private fun updateText(block: suspend Text.() -> Text) {
        withCurrent {
            this@TextInputViewModelImpl.text.emit(block(this))
        }
    }

    private fun withCurrent(block: suspend Text.() -> Unit) {
        viewModelScope.launch {
            val current = (state.value as? State.Loaded)?.text ?: return@launch
            block(current)
        }
    }

}