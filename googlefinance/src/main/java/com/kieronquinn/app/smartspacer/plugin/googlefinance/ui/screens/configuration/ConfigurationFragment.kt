package com.kieronquinn.app.smartspacer.plugin.googlefinance.ui.screens.configuration

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.googlefinance.R
import com.kieronquinn.app.smartspacer.plugin.googlefinance.targets.GoogleFinanceTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.googlefinance.targets.GoogleFinanceTarget.TargetData.MinimumTrendDirection
import com.kieronquinn.app.smartspacer.plugin.googlefinance.ui.screens.configuration.ConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.googlefinance.ui.screens.configuration.minimumtrend.MinimumTrendFragment.Companion.setupMinimumTrendResultListener
import com.kieronquinn.app.smartspacer.plugin.googlefinance.utils.extensions.parseTrend
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class ConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    private val viewModel by viewModel<ConfigurationViewModel>()

    private val reconfigureLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) {
        //We don't actually care about the result
    }

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupState()
        viewModel.setup(requireActivity().intent.getStringExtra(EXTRA_SMARTSPACER_ID)!!)
    }

    private fun setupListeners() {
        setupMinimumTrendResultListener {
            viewModel.onMinimumTrendChanged(it.takeIf { it.isNotEmpty() }?.parseTrend())
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
            }
            is State.Loaded -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return listOfNotNull(
            Setting(
                getString(R.string.target_configuration_reconfigure_title),
                getString(R.string.target_configuration_reconfigure_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_reconfigure)
            ) {
                viewModel.onReconfigureClicked(requireContext(), reconfigureLauncher)
            },
            Setting(
                getString(R.string.target_configuration_minimum_trend_title),
                targetData.getMinimumTrendContent(),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_minimum_trend),
                onClick = viewModel::onMinimumTrendClicked
            ),
            Dropdown(
                getString(R.string.target_configuration_minimum_trend_direction_title),
                getString(targetData.minimumTrendDirection.label),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_minimum_trend_direction),
                targetData.minimumTrendDirection,
                viewModel::onMinimumTrendDirectionChanged,
                MinimumTrendDirection.values().toList()
            ) {
                it.label
            }.takeIf { targetData.minimumTrend != null },
            SwitchSetting(
                targetData.filterExpanded,
                getString(R.string.target_configuration_filter_expanded_title),
                getString(R.string.target_configuration_filter_expanded_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_filter_expanded),
                onChanged = viewModel::onFilterExpandedChanged
            ).takeIf { targetData.minimumTrend != null },
            Setting(
                getString(R.string.target_configuration_clear_dismissed_title),
                getString(R.string.target_configuration_clear_dismissed_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_clear_dismissed),
                onClick = viewModel::onClearDismissedClicked
            )
        )
    }

    private fun TargetData.getMinimumTrendContent(): String {
        return getString(
            R.string.target_configuration_minimum_trend_content,
            minimumTrend?.toString()?.let { "$it%" }
                ?: getString(R.string.target_configuration_minimum_trend_none)
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}