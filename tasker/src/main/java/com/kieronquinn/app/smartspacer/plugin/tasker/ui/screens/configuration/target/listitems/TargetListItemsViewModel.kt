package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.listitems

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.ListItems.Companion.defaultListItem
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions.modify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class TargetListItemsViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(current: List<Text>)
    abstract fun dismiss()
    abstract fun onItemClicked(key: String, item: Text)
    abstract fun onItemChanged(index: Int, item: Text)
    abstract fun onItemDeleted(index: Int)
    abstract fun onAddClicked(context: Context)

    sealed class State {
        object Loading: State()
        data class Loaded(val items: List<Text>, val showFab: Boolean): State()
    }

}

class TargetListItemsViewModelImpl(
    private val navigation: ContainerNavigation
): TargetListItemsViewModel() {

    companion object {
        const val MAX_ITEMS = 3
    }

    private val items = MutableStateFlow<List<Text>?>(null)

    override val state = items.filterNotNull().mapLatest {
        State.Loaded(it, it.size < MAX_ITEMS)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(current: List<Text>) {
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

    override fun onItemClicked(key: String, item: Text) {
        viewModelScope.launch {
            navigation.navigate(TargetListItemsFragmentDirections.actionGlobalNavGraphIncludeText(
                TextInputFragment.Config(
                    R.string.configuration_target_list_items_content_generic,
                    key,
                    item
                )
            ))
        }
    }

    override fun onItemChanged(index: Int, item: Text) {
        val current = items.value ?: return
        viewModelScope.launch {
            items.emit(current.modify(index) { item })
        }
    }

    override fun onItemDeleted(index: Int) {
        val current = items.value ?: return
        val itemToDelete = current.getOrNull(index) ?: return
        viewModelScope.launch {
            items.emit(current.minus(itemToDelete))
        }
    }

    override fun onAddClicked(context: Context) {
        val current = items.value ?: return
        val new = defaultListItem(context, current.size + 1)
        viewModelScope.launch {
            items.emit(current.plus(new))
        }
    }

}