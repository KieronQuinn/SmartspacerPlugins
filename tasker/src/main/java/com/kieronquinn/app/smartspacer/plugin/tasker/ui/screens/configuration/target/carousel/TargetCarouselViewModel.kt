package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.carousel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Carousel.CarouselItem
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Carousel.Companion.defaultCarouselItem
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.modify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetCarouselViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(items: List<CarouselItem>)
    abstract fun dismiss()
    abstract fun onAddClicked(context: Context)
    abstract fun onDeleteClicked(index: Int)
    abstract fun onUpperTextClicked(key: String, current: Text)
    abstract fun onUpperTextChanged(index: Int, text: Text)
    abstract fun onIconClicked(key: String, current: Icon)
    abstract fun onIconChanged(index: Int, icon: Icon)
    abstract fun onTapActionClicked(key: String, current: TapAction)
    abstract fun onTapActionChanged(index: Int, tapAction: TapAction)
    abstract fun onLowerTextClicked(key: String, current: Text)
    abstract fun onLowerTextChanged(index: Int, text: Text)

    sealed class State {
        object Loading: State()
        data class Loaded(val items: List<CarouselItem>, val showFab: Boolean): State()
    }

}

class TargetCarouselViewModelImpl(
    private val navigation: ContainerNavigation
): TargetCarouselViewModel() {

    companion object {
        const val MAX_ITEMS = 4
    }

    private val items = MutableStateFlow<List<CarouselItem>?>(null)

    override val state = items.filterNotNull().mapLatest {
        State.Loaded(it, it.size < MAX_ITEMS)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(items: List<CarouselItem>) {
        if(this.items.value != null) return
        viewModelScope.launch {
            this@TargetCarouselViewModelImpl.items.emit(items)
        }
    }

    override fun dismiss() {
        viewModelScope.launch {
            navigation.navigateBack()
        }
    }

    override fun onAddClicked(context: Context) {
        val current = items.value ?: return
        viewModelScope.launch {
            val new = defaultCarouselItem(context)
            items.emit(current.plus(new))
        }
    }

    override fun onDeleteClicked(index: Int) {
        val current = items.value ?: return
        val itemToRemove = current.getOrNull(index) ?: return
        viewModelScope.launch {
            items.emit(current.minus(itemToRemove))
        }
    }

    override fun onUpperTextClicked(key: String, current: Text) {
        viewModelScope.launch {
            navigation.navigate(TargetCarouselFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_carousel_item_upper_title,
                    key,
                    current
                )
            ))
        }
    }

    override fun onUpperTextChanged(index: Int, text: Text) {
        val current = items.value ?: return
        viewModelScope.launch {
            items.emit(current.modify(index) {
                it.copy(upperText = text)
            })
        }
    }

    override fun onIconClicked(key: String, current: Icon) {
        viewModelScope.launch {
            navigation.navigate(TargetCarouselFragmentDirections.actionGlobalNavGraphIncludeIcon(
                IconPickerFragment.Config(
                    R.string.configuration_carousel_item_icon_title,
                    key,
                    current
                )
            ))
        }
    }

    override fun onIconChanged(index: Int, icon: Icon) {
        val current = items.value ?: return
        viewModelScope.launch {
            items.emit(current.modify(index) {
                it.copy(image = icon)
            })
        }
    }

    override fun onLowerTextClicked(key: String, current: Text) {
        viewModelScope.launch {
            navigation.navigate(TargetCarouselFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_carousel_item_lower_title,
                    key,
                    current
                )
            ))
        }
    }

    override fun onLowerTextChanged(index: Int, text: Text) {
        val current = items.value ?: return
        viewModelScope.launch {
            items.emit(current.modify(index) {
                it.copy(lowerText = text)
            })
        }
    }

    override fun onTapActionClicked(key: String, current: TapAction) {
        viewModelScope.launch {
            navigation.navigate(TargetCarouselFragmentDirections.actionGlobalNavGraphIncludeTapAction(
                TapActionFragment.Config(
                    R.string.tap_action_title_generic,
                    key,
                    current
                )
            ))
        }
    }

    override fun onTapActionChanged(index: Int, tapAction: TapAction) {
        val current = items.value ?: return
        viewModelScope.launch {
            items.emit(current.modify(index) {
                it.copy(tapAction = tapAction)
            })
        }
    }

}