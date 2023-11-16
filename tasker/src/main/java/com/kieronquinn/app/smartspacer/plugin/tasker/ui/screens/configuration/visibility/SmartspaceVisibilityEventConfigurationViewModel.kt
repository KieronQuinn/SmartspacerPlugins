package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.visibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SmartspaceVisibilityEventConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(visible: Boolean?)
    abstract fun onVisibilityChanged(visibility: Visibility)

    sealed class State {
        data object Loading: State()
        data class Loaded(val visibility: Visibility): State()
    }

    enum class Visibility(val label: Int) {
        VISIBLE(R.string.smartspace_visibility_event_title_visibility_content_visible),
        INVISIBLE(R.string.smartspace_visibility_event_title_visibility_content_invisible);

        companion object {
            fun fromBoolean(visible: Boolean): Visibility {
                return if(visible) VISIBLE else INVISIBLE
            }
        }

        fun toBoolean(): Boolean {
            return when(this) {
                VISIBLE -> true
                INVISIBLE -> false
            }
        }
    }

}

class SmartspaceVisibilityEventConfigurationViewModelImpl: SmartspaceVisibilityEventConfigurationViewModel() {

    private val baseVisibility = MutableStateFlow<Visibility?>(null)
    private val setVisibility = MutableStateFlow<Visibility?>(null)

    override val state = combine(
        baseVisibility.filterNotNull(),
        setVisibility
    ) { base, set ->
        State.Loaded(set ?: base)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(visible: Boolean?) {
        val visibility = visible?.let { Visibility.fromBoolean(it) } ?: Visibility.VISIBLE
        viewModelScope.launch {
            baseVisibility.emit(visibility)
        }
    }

    override fun onVisibilityChanged(visibility: Visibility) {
        viewModelScope.launch {
            setVisibility.emit(visibility)
        }
    }

}