package com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.googlehealth.GoogleHealthPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.googlehealth.R
import com.kieronquinn.app.smartspacer.plugin.googlehealth.complications.GoogleHealthComplication.ComplicationData.TapAction
import com.kieronquinn.app.smartspacer.plugin.googlehealth.complications.GoogleHealthComplication.ComplicationData.Timeout
import com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens.HealthConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens.focus.HealthConfigurationFocusFragment.Companion.setupMetricListener
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class HealthConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override val adapter by lazy {
        Adapter()
    }
    
    private val viewModel by viewModel<HealthConfigurationViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = requireActivity().intent.getStringExtra(SmartspacerConstants.EXTRA_SMARTSPACER_ID)
        viewModel.setupWithId(id ?: return)
        setupState()
        setupMetricListener {
            viewModel.onMetricChanged(it)
            requireActivity().setResult(Activity.RESULT_OK)
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
    
    private fun handleState(state: State) {
        when(state) {
            is State.Loading -> {
                binding.settingsBaseLoading.isVisible = true
                binding.settingsBaseRecyclerView.isVisible = false
            }
            is State.Loaded -> {
                binding.settingsBaseLoading.isVisible = false
                binding.settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), binding.settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return listOfNotNull(
            if (!metricAvailable) {
                Card(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_error),
                    getString(R.string.configuration_focus_no_longer_available),
                    onClick = ::launchGoogleHealth
                )
            } else null,
            GenericSettingsItem.Setting(
                getString(R.string.configuration_health_focus),
                selected?.label?.let { getString(it) }
                    ?: getString(R.string.metric_unset),
                icon = null,
                onClick = viewModel::onMetricClicked
            ),
            Dropdown(
                getString(R.string.configuration_tap_action),
                getString(action.label),
                null,
                action,
                onSet = viewModel::onTapActionChanged,
                TapAction.entries
            ) {
                it.label
            },
            SwitchSetting(
                timeoutEnabled,
                getString(R.string.configuration_health_timeout),
                getString(R.string.configuration_health_timeout_content),
                icon = null,
                onChanged = viewModel::onTimeoutEnabledChanged
            ),
            if(timeoutEnabled) {
                Dropdown(
                    getString(R.string.configuration_health_timeout),
                    getString(timeout.label),
                    null,
                    timeout,
                    onSet = viewModel::onTimeoutChanged,
                    Timeout.entries
                ) {
                    it.label
                }
            } else null
        )
    }

    private fun launchGoogleHealth() {
        requireContext().packageManager.getLaunchIntentForPackage(PACKAGE_NAME)?.let {
            startActivity(it)
        }
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}