package com.kieronquinn.app.smartspacer.plugins.datausage.ui.screens.configuration

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.datepicker.MaterialDatePicker
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugins.datausage.R
import com.kieronquinn.app.smartspacer.plugins.datausage.complications.DataUsageComplication.ComplicationData.RefreshRate
import com.kieronquinn.app.smartspacer.plugins.datausage.ui.screens.configuration.ConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugins.datausage.utils.extensions.getDayOrMax
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import com.kieronquinn.app.shared.R as SharedR

class ConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    private val viewModel by viewModel<ConfigurationViewModel>()

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        viewModel.setup(requireActivity().intent.getStringExtra(EXTRA_SMARTSPACER_ID)!!)
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
                requireActivity().setResult(Activity.RESULT_OK)
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        if(networkStatus == null) {
            return listOf(
                Card(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_data_usage),
                    getString(R.string.configuration_permission_required),
                    viewModel::onPermissionClicked
                )
            )
        }
        return listOf(
            Dropdown(
                getString(R.string.configuration_network_type_title),
                getString(data.network.label),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_data_usage),
                data.network,
                viewModel::onNetworkChanged,
                networkStatus.getValidNetworks(),
            ) {
                it.label
            },
            Setting(
                getString(R.string.configuration_data_usage_cycle_day_title),
                getString(R.string.configuration_data_usage_cycle_day_description, data.cycleDay),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_cycle_day)
            ) {
                onCycleDayClicked(data.cycleDay)
            },
            Dropdown(
                getString(R.string.configuration_refresh_rate_title),
                getString(data.refreshRate.label),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_refresh_rate),
                data.refreshRate,
                viewModel::onRefreshRateChanged,
                RefreshRate.values().toList()
            ) {
                it.label
            }
        )
    }

    private fun onCycleDayClicked(current: Int) {
        val timestamp = LocalDate.now().let {
            it.withDayOfMonth(it.month.getDayOrMax(current))
        }.atStartOfDay(ZoneId.systemDefault()).plusHours(12).toInstant().toEpochMilli()
        MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.configuration_data_usage_cycle_day_title))
            .setSelection(timestamp)
            .setPositiveButtonText(android.R.string.ok)
            .setNegativeButtonText(android.R.string.cancel)
            .build().apply {
                addOnPositiveButtonClickListener {
                    val cycleDay = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault())
                        .toLocalDate().dayOfMonth
                    viewModel.onCycleDayChanged(cycleDay)
                }
            }.show(childFragmentManager, "date_picker")
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}