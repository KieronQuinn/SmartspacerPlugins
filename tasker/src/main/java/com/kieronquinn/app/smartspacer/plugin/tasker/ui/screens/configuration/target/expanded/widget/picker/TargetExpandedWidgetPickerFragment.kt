package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.picker

import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesOverflow
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.repositories.WidgetRepository
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSearchFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.picker.TargetExpandedWidgetPickerViewModel.Item
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.picker.TargetExpandedWidgetPickerViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.Companion.setupStringResultListener
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel

class TargetExpandedWidgetPickerFragment: BaseSearchFragment(), BackAvailable, ProvidesOverflow {

    companion object {
        private const val KEY_COMPONENT_NAME = "component_name"
        private const val KEY_LABEL = "label"
        const val REQUEST_KEY_VARIABLE = "widget_variable"

        fun Fragment.setupExpandedWidgetPickerResultListener(
            key: String,
            callback: (componentName: String, label: String) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val componentName = bundle.getString(KEY_COMPONENT_NAME)
                    ?: return@setFragmentResultListener
                val label = bundle.getString(KEY_LABEL)
                    ?: return@setFragmentResultListener
                callback.invoke(componentName, label)
            }
        }
    }

    override val viewModel by viewModel<TargetExpandedWidgetPickerViewModel>()
    private val args by navArgs<TargetExpandedWidgetPickerFragmentArgs>()

    private val config by lazy {
        args.config as Config
    }

    override val adapter by lazy {
        Adapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupState()
    }

    override fun inflateMenu(menuInflater: MenuInflater, menu: Menu) {
        menuInflater.inflate(R.menu.menu_variable, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.menu_variable -> viewModel.onVariableClicked(config.currentComponent)
        }
        return true
    }

    private fun setupListener() {
        setupStringResultListener(REQUEST_KEY_VARIABLE) {
            whenResumed {
                dismiss(it, it)
            }
        }
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) = with(binding) {
        when(state) {
            is State.Loading -> {
                settingsBaseLoading.isVisible = true
                settingsBaseRecyclerView.isVisible = false
                includeSearch.root.isVisible = false
            }
            is State.Loaded -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                includeSearch.root.isVisible = true
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return items.map {
            when(it) {
                is Item.App -> {
                    Header(it.label)
                }
                is Item.Widget -> {
                    Setting(
                        it.widget.label,
                        it.widget.subtitle ?: "",
                        icon = null
                    ) {
                        onWidgetClicked(it.widget)
                    }
                }
            }
        }
    }

    private fun onWidgetClicked(widget: WidgetRepository.Widget) {
        dismiss(widget.componentName, "${widget.label} (${widget.subtitle})")
    }

    private fun dismiss(componentName: String, label: String) {
        setFragmentResult(config.key, bundleOf(
            KEY_COMPONENT_NAME to componentName,
            KEY_LABEL to label
        ))
        viewModel.dismiss()
    }

    @Parcelize
    data class Config(
        val key: String,
        val currentComponent: String?
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}