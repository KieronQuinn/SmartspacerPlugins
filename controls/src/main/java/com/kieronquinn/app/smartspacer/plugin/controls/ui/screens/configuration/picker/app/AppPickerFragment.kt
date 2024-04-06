package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.app

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.app.AppPickerViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class AppPickerFragment: BaseSettingsFragment(), BackAvailable {

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val viewModel by viewModel<AppPickerViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
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
            }
            is State.Loaded -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return apps.map {
            Setting(
                it.name,
                it.componentName.packageName,
                it.icon.loadDrawable(requireContext()),
                tintIcon = false
            ) {
                viewModel.onAppClicked(it)
            }
        }.ifEmpty {
            listOf(Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_error),
                getString(R.string.configuration_app_picker_empty)
            ))
        }
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}