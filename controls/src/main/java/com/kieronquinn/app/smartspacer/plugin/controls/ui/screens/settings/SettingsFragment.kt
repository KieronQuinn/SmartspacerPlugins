package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.settings

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsSettingsRepository.RefreshPeriod
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.settings.SettingsViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.whenResumed
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class SettingsFragment: BaseSettingsFragment(), BackAvailable {

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val viewModel by viewModel<SettingsViewModel>()

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
        return listOf(
            Dropdown(
                getString(R.string.settings_refresh_period_title),
                getString(
                    R.string.settings_refresh_period_content,
                    getString(refreshPeriod.labelRes)
                ),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_loading),
                refreshPeriod,
                viewModel::onRefreshPeriodChanged,
                RefreshPeriod.entries
            ) {
                it.labelRes
            },
            SwitchSetting(
                refreshOnScreenStateChanged,
                getString(R.string.settings_refresh_on_screen_state_changed_title),
                getString(R.string.settings_refresh_on_screen_state_changed_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_device_lock),
                onChanged = viewModel::onRefreshOnScreenStateChanged
            )
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}