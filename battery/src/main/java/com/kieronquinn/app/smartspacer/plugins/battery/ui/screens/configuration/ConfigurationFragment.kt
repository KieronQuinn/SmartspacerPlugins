package com.kieronquinn.app.smartspacer.plugins.battery.ui.screens.configuration

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugins.battery.R
import com.kieronquinn.app.smartspacer.plugins.battery.model.BatteryLevels
import com.kieronquinn.app.smartspacer.plugins.battery.ui.screens.configuration.ConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import org.koin.androidx.viewmodel.ext.android.viewModel
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
            Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_info),
                getString(R.string.configuration_info)
            ),
            Header(getString(R.string.configuration_header_devices)),
            *batteryLevels?.levels?.map {
                Setting(
                    it.name,
                    getLabel(it),
                    BitmapDrawable(resources, it.icon),
                    onLongClick = { viewModel.onDeviceLongClicked(it.name) }
                ) {
                    viewModel.onDeviceClicked(it)
                }
            }?.toTypedArray() ?: emptyArray(),
            Header(getString(R.string.configuration_header_options)),
            SwitchSetting(
                complicationData.showWhenDisconnected,
                getString(R.string.configuration_show_when_disconnected_title),
                getString(R.string.configuration_show_when_disconnected_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_battery),
                onChanged = viewModel::onShowWhenDisconnectedChanged
            )
        )
    }

    private fun State.Loaded.getLabel(batteryLevel: BatteryLevels.BatteryLevel): String {
        return if(complicationData.name == batteryLevel.name) {
            getString(
                R.string.complication_configuration_selected,
                batteryLevel.getLabel(requireContext())
            )
        }else batteryLevel.getLabel(requireContext())
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}