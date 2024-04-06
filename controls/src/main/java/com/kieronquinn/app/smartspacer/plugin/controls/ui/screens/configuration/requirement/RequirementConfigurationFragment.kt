package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.requirement

import android.app.Activity
import android.os.Bundle
import android.service.controls.Control
import android.service.controls.templates.ControlTemplate
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates.ControlsTemplate
import com.kieronquinn.app.smartspacer.plugin.controls.model.LoadingConfig
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.control.ControlPickerFragment.Companion.setupControlResultListener
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.requirement.RequirementConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class RequirementConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val viewModel by viewModel<RequirementConfigurationViewModel>()

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if(!it) {
            viewModel.onShowAppInfo()
        }
        viewModel.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupState()
        viewModel.setup(requireActivity().intent.getStringExtra(EXTRA_SMARTSPACER_ID)!!)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun setupListeners() {
        setupControlResultListener {
            viewModel.onControlChanged(it)
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
            is State.Incompatible -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(listOf(Card(
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_error),
                    getString(R.string.configuration_incompatible)
                )), settingsBaseRecyclerView)
            }
            is State.Loaded -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
                if(state.data.controlId != null && state.hasDisabledBatteryOptimisation) {
                    requireActivity().setResult(Activity.RESULT_OK)
                }
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        val name = if(data.controlName != null && data.controlApp != null) {
            getString(
                R.string.configuration_select_control_content_set, data.controlName, data.controlApp
            )
        }else null
        val specificOptions = control?.let {
            val template = ControlsTemplate.getTemplate(it.controlTemplate)
            template.getTemplateSpecificOptions(it, data)
        } ?: emptyList()
        return listOfNotNull(
            Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                getString(R.string.configuration_disable_battery_optimisation),
                viewModel::onDisableBatteryOptimisationClicked
            ).takeIf { !hasDisabledBatteryOptimisation },
            Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                getString(R.string.configuration_grant_notification_permission),
            ) {
                viewModel.onNotificationPermissionClicked(notificationPermission)
            }.takeIf { hasDisabledBatteryOptimisation && !hasNotificationPermission },
            Card(
                ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                getText(R.string.requirement_warning)
            ),
            Setting(
                getString(R.string.configuration_select_control_title),
                name ?: getString(R.string.configuration_select_control_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_controls),
                onClick = viewModel::onSelectControlClicked
            ).takeIf { hasDisabledBatteryOptimisation }
        ) + specificOptions
    }

    private fun ControlsTemplate<ControlTemplate>.getTemplateSpecificOptions(
        control: Control,
        data: ControlsRequirement.RequirementData
    ): List<BaseSettingsItem> {
        val availableTypes = getAvailableRequirementTypes()
        val interactions = ControlsTemplate.ExtraRequirementOptionsInteractions(
            viewModel::onBooleanValueChanged,
            viewModel::onModeValueChanged,
            viewModel::onRequirementValueTypeChanged,
            viewModel::onFloatValueChanged,
        )
        return listOf(
            Header(getString(R.string.configuration_options)),
            Dropdown(
                getString(R.string.configuration_loading_config_title),
                getString(
                    R.string.configuration_loading_config_content_alt,
                    getString(data.loadConfig.labelAlt)
                ),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_loading),
                data.loadConfig,
                viewModel::onLoadingConfigChanged,
                LoadingConfig.entries.filterNot { it == LoadingConfig.HIDDEN }
            ) {
                it.labelAlt
            },
            Dropdown(
                getString(R.string.requirement_type_title),
                getString(
                    R.string.requirement_type_content,
                    getString(data.controlRequirementType.label)
                ),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_tap_action),
                data.controlRequirementType,
                viewModel::onRequirementTypeChanged,
                availableTypes
            ) {
                it.label
            }
        ) + getExtraRequirementOptions(
            requireContext(), control, data, interactions
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}