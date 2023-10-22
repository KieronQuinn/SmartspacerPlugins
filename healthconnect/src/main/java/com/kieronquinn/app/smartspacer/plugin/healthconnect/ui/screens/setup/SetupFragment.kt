package com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.setup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.health.connect.client.PermissionController
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType
import com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.setup.SetupViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getSerializableExtraCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenCreated
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider.Companion.EXTRA_SMARTSPACER_ID
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class SetupFragment: BaseSettingsFragment(), BackAvailable {

    companion object {
        private const val KEY_DATA_TYPE = "data_type"
        private const val KEY_AUTHORITY = "authority"

        fun applyConfig(intent: Intent, dataType: DataType, authority: String): Intent {
            return intent.apply {
                putExtra(KEY_DATA_TYPE, dataType)
                putExtra(KEY_AUTHORITY, authority)
            }
        }
    }

    private val viewModel by viewModel<SetupViewModel>()
    private var hasLaunchedPrompt = false

    private val requestPermissions = registerForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { perms ->
        viewModel.checkPermissions()
    }

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if(!result) {
            viewModel.showNotificationSettings()
        }
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override val adapter by lazy {
        Adapter()
    }

    private val smartspacerId by lazy {
        requireActivity().intent.getStringExtra(EXTRA_SMARTSPACER_ID)!!
    }

    private val authority by lazy {
        requireActivity().intent.getStringExtra(KEY_AUTHORITY)!!
    }

    private val dataType by lazy {
        requireActivity().intent.getSerializableExtraCompat(KEY_DATA_TYPE, DataType::class.java)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.setup(dataType, smartspacerId, authority)
        setupState()
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkPermissions()
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenCreated {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) = with(binding) {
        when(state) {
            is State.Loading -> {
                settingsBaseLoadingLabel.setText(R.string.setup_loading)
                settingsBaseLoading.isVisible = true
                settingsBaseRecyclerView.isVisible = false
            }
            is State.RequestPermissions -> {
                settingsBaseLoadingLabel.setText(R.string.setup_requesting)
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.getPermissionRequestItems(), settingsBaseRecyclerView)
                if(hasLaunchedPrompt) return@with
                hasLaunchedPrompt = true
                requestPermissions.launch(state.permissions)
            }
            is State.Saving -> {
                settingsBaseLoadingLabel.setText(R.string.setup_saving)
                settingsBaseLoading.isVisible = true
                settingsBaseRecyclerView.isVisible = false
            }
            is State.Dismiss -> {
                settingsBaseLoadingLabel.setText(R.string.setup_loading)
                settingsBaseLoading.isVisible = true
                settingsBaseRecyclerView.isVisible = false
                requireActivity().setResult(state.result)
                requireActivity().finish()
            }
        }
    }

    private fun State.RequestPermissions.getPermissionRequestItems(): List<BaseSettingsItem> {
        val permissionLabels = getString(dataType.label)
        return listOfNotNull(
            GenericSettingsItem.Setting(
                getString(R.string.setup_request_permission_title),
                getString(R.string.setup_request_permission_content, permissionLabels),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_health_connect_tinted),
                onClick = viewModel::onOpenHealthConnectClicked,
                isEnabled = !hasGrantedHealthConnectPermissions
            ),
            GenericSettingsItem.SwitchSetting(
                hasDisabledBatteryOptimisation,
                getString(R.string.setup_request_disable_battery_optimisation_title),
                getString(R.string.setup_request_disable_battery_optimisation_content),
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_setup_disable_battery_optimisation
                ),
                enabled = !hasDisabledBatteryOptimisation
            ) {
                viewModel.onBatteryOptimisationClicked()
            },
            if(requiresNotificationPermission) {
                GenericSettingsItem.SwitchSetting(
                    hasEnabledNotifications,
                    getString(R.string.setup_request_notification_permission_title),
                    getString(R.string.setup_request_notification_permission_content),
                    ContextCompat.getDrawable(requireContext(), R.drawable.ic_setup_notifcations),
                    enabled = !hasEnabledNotifications
                ) {
                    viewModel.onNotificationsClicked(notificationPermission)
                }
            }else null
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}