package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.images

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.modify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetImagesViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: List<Icon>)
    abstract fun dismiss()
    abstract fun onItemDeleteClicked(index: Int)
    abstract fun onItemClicked(key: String, item: Icon)
    abstract fun onItemChanged(index: Int, item: Icon)
    abstract fun onAddClicked(context: Context)

    sealed class State {
        object Loading: State()
        data class Loaded(val items: List<Icon>): State()
    }

}

class TargetImagesViewModelImpl(
    private val navigation: ContainerNavigation
): TargetImagesViewModel() {

    private val items = MutableStateFlow<List<Icon>?>(null)

    override val state = items.filterNotNull().mapLatest {
        State.Loaded(it)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: List<Icon>) {
        if(items.value != null) return
        viewModelScope.launch {
            items.emit(current)
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onItemClicked(key: String, item: Icon) {
        viewModelScope.launch {
            navigation.navigate(TargetImagesFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_images_image_title_generic,
                    key,
                    item,
                    showBuiltIn = false,
                    showTint = false
                )
            ))
        }
    }

    override fun onAddClicked(context: Context) {
        val current = items.value ?: return
        viewModelScope.launch {
            items.emit(current.plus(Icon.createDefaultImage(context)))
        }
    }

    override fun onItemChanged(index: Int, item: Icon) {
        val current = items.value ?: return
        viewModelScope.launch {
            items.emit(current.modify(index) { item })
        }
    }

    override fun onItemDeleteClicked(index: Int) {
        val current = items.value ?: return
        val itemToRemove = current.getOrNull(index) ?: return
        viewModelScope.launch {
            items.emit(current.minus(itemToRemove))
        }
    }

}