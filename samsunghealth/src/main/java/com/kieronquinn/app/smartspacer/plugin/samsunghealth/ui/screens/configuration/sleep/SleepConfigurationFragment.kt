package com.kieronquinn.app.smartspacer.plugin.samsunghealth.ui.screens.configuration.sleep

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.R
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.complications.SleepComplication.ComplicationData.Timeout
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.ui.screens.configuration.sleep.SleepConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class SleepConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val viewModel by viewModel<SleepConfigurationViewModel>()

    override val adapter by lazy {
        Adapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = requireActivity().intent.getStringExtra(SmartspacerConstants.EXTRA_SMARTSPACER_ID)
        viewModel.setupWithId(id ?: return)
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
            GenericSettingsItem.SwitchSetting(
                timeoutEnabled,
                getString(R.string.configuration_sleep_timeout),
                getString(R.string.configuration_sleep_timeout_content),
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_configuration_sleep_timeout
                ),
                onChanged = viewModel::onTimeoutEnabledChanged
            ),
            if(timeoutEnabled) {
                GenericSettingsItem.Dropdown(
                    getString(R.string.configuration_sleep_timeout),
                    getString(timeout.label),
                    ContextCompat.getDrawable(
                        requireContext(), R.drawable.ic_configuration_sleep_timeout
                    ),
                    timeout,
                    onSet = viewModel::onTimeoutChanged,
                    Timeout.values().toList()
                ) {
                    it.label
                }
            }else null
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}