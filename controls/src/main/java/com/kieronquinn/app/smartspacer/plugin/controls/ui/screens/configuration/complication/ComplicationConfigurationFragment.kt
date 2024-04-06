package com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.complication

import android.app.Activity
import android.os.Bundle
import android.service.controls.Control
import android.service.controls.templates.ControlTemplate
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.complications.ControlsComplication
import com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates.ControlsTemplate
import com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates.ControlsTemplate.ExtraOptionsInteractions
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.model.LoadingConfig
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.complication.ComplicationConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.icon.IconPickerFragment.Companion.setupIconResultListener
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.picker.control.ControlPickerFragment.Companion.setupControlResultListener
import com.kieronquinn.app.smartspacer.plugin.controls.ui.screens.configuration.title.CustomTitleBottomSheetFragment.Companion.setupCustomTitleResultListener
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesOverflow
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class ComplicationConfigurationFragment: BaseSettingsFragment(), BackAvailable, ProvidesOverflow {

    companion object {
        private const val REQUEST_KEY_ICON = "complication_icon"
    }

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val viewModel by viewModel<ComplicationConfigurationViewModel>()

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

    override fun inflateMenu(menuInflater: MenuInflater, menu: Menu) {
        menuInflater.inflate(R.menu.menu_settings, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId) {
            R.id.menu_settings -> viewModel.onSettingsClicked()
        }
        return true
    }

    private fun setupListeners() {
        setupControlResultListener {
            viewModel.onControlChanged(it)
        }
        setupIconResultListener(REQUEST_KEY_ICON) {
            viewModel.onIconChanged(it)
        }
        setupCustomTitleResultListener {
            viewModel.onCustomTitleChanged(it)
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
            template.getTemplateSpecificOptions(it, data.controlTapAction, data, panelAvailable)
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
        tapAction: ControlTapAction,
        data: ControlsComplication.ComplicationData,
        panelAvailable: Boolean
    ): List<BaseSettingsItem> {
        val availableActions = getAvailableControlTapActions().filterNot {
            it == ControlTapAction.OPEN_PANEL && !panelAvailable
        }
        val controlExtraData = ControlExtraData(
            data.doesRequireUnlock(control),
            data.modeSetMode,
            data.floatSetFloat,
            data.shouldHideDetails
        )
        val interactions = ExtraOptionsInteractions(
            viewModel::onFloatValueChanged,
            viewModel::onModeChanged,
            viewModel::onHideDetailsChanged
        )
        return listOf(
            Header(getString(R.string.configuration_options)),
            Dropdown(
                getString(R.string.configuration_loading_config_title),
                getString(
                    R.string.configuration_loading_config_content,
                    getString(data.loadConfig.label)
                ),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_loading),
                data.loadConfig,
                viewModel::onLoadingConfigChanged,
                LoadingConfig.entries
            ) {
                it.label
            },
            Dropdown(
                getString(R.string.configuration_tap_action_title),
                getString(
                    R.string.configuration_tap_action_content,
                    getString(data.controlTapAction.label)
                ),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_tap_action),
                data.controlTapAction,
                viewModel::onTapActionChanged,
                availableActions
            ) {
                it.label
            },
            SwitchSetting(
                data.doesRequireUnlock(control),
                getString(R.string.configuration_requires_unlock_title),
                getText(R.string.configuration_requires_unlock_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_requires_unlock),
                onChanged = viewModel::onRequireUnlockChanged
            ),
        ) + getExtraOptions(
            requireContext(), control, tapAction, controlExtraData, interactions
        ) + listOf(
            Header(getString(R.string.configuration_customisation_header)),
            Setting(
                getString(R.string.configuration_custom_icon_title),
                data.customIcon?.describe(requireContext())
                    ?: getText(R.string.configuration_custom_icon_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_custom_icon)
            ) {
                viewModel.onIconClicked(REQUEST_KEY_ICON)
            },
            Setting(
                getString(R.string.configuration_custom_title_title),
                data.customTitle
                    ?: getText(R.string.configuration_custom_title_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_configuration_custom_title),
                onClick = viewModel::onCustomTitleClicked
            )
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}