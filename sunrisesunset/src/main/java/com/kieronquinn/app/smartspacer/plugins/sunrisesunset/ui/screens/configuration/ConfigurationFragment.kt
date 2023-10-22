package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.R
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository.ShowFor
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.ui.screens.configuration.ConfigurationViewModel.State
import com.kieronquinn.app.shared.R as SharedR

abstract class ConfigurationFragment: BaseSettingsFragment(), BackAvailable, ProvidesTitle {

    private val permissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.onResume()
    }

    abstract val viewModel: ConfigurationViewModel

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
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
        val show = hasLocationPermission && hasAlarmPermission && hasBackgroundLocationPermission
        return listOfNotNull(
            Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                getString(R.string.configuration_location_permission),
                ::onLocationPermissionClicked
            ).takeIf { !hasLocationPermission },
            Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                getString(R.string.configuration_background_location_permission),
                ::onBackgroundLocationPermissionClicked
            ).takeIf { hasLocationPermission && !hasBackgroundLocationPermission },
            Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                getString(R.string.configuration_alarm_permission),
                viewModel::onAlarmPermissionClicked
            ).takeIf { !hasAlarmPermission },
            Dropdown(
                getString(R.string.configuration_show_before_title),
                getString(
                    R.string.configuration_show_before_content,
                    getTitle(),
                    getString(state.showBefore.label)
                ),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_before),
                state.showBefore,
                viewModel::onShowBeforeChanged,
                ShowFor.values().toList()
            ) {
                it.label
            }.takeIf { show },
            Dropdown(
                getString(R.string.configuration_show_after_title),
                getString(
                    R.string.configuration_show_after_content,
                    getTitle(),
                    getString(state.showAfter.label)
                ),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_after),
                state.showAfter,
                viewModel::onShowAfterChanged,
                ShowFor.values().toList()
            ) {
                it.label
            }.takeIf { show },
            Setting(
                getString(R.string.configuration_refresh_location),
                getString(R.string.configuration_refresh_location_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_refresh),
                onClick = viewModel::onRefreshClicked
            ).takeIf { show }
        )
    }

    private fun onLocationPermissionClicked() {
        permissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun onBackgroundLocationPermissionClicked() {
        permissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}