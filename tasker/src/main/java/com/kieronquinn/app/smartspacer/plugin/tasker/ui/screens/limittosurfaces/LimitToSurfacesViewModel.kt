package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.limittosurfaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.sdk.model.UiSurface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class LimitToSurfacesViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: List<UiSurface>)
    abstract fun dismiss()
    abstract fun onSurfaceChanged(surface: UiSurface, enabled: Boolean)

    sealed class State {
        object Loading: State()
        data class Loaded(val limitToSurfaces: Set<UiSurface>): State()
    }

}

class LimitToSurfacesViewModelImpl(
    private val navigation: ContainerNavigation
): LimitToSurfacesViewModel() {

    private val surfaces = MutableStateFlow<Set<UiSurface>?>(null)

    override val state = surfaces.filterNotNull().map {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: List<UiSurface>) {
        if(surfaces.value != null) return
        viewModelScope.launch {
            surfaces.emit(current.toSet())
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onSurfaceChanged(surface: UiSurface, enabled: Boolean) {
        val current = surfaces.value ?: return
        viewModelScope.launch {
            if(enabled){
                surfaces.emit(current.plus(surface))
            }else{
                surfaces.emit(current.minus(surface))
            }
        }
    }

}