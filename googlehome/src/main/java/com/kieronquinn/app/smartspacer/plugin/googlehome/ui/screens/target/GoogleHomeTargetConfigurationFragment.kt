package com.kieronquinn.app.smartspacer.plugin.googlehome.ui.screens.target

import android.app.ActivityOptions
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.googlehome.R
import com.kieronquinn.app.smartspacer.plugin.googlehome.model.HideMode
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class GoogleHomeTargetConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override val adapter by lazy {
        Adapter()
    }

    private val viewModel by viewModel<GoogleHomeTargetConfigurationViewModel>()

    private val id by lazy {
        requireActivity().intent.getStringExtra(SmartspacerConstants.EXTRA_SMARTSPACER_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    private fun handleState(state: GoogleHomeTargetConfigurationViewModel.State) {
        when(state) {
            is GoogleHomeTargetConfigurationViewModel.State.Loading -> {
                binding.settingsBaseLoading.isVisible = true
                binding.settingsBaseRecyclerView.isVisible = false
            }
            is GoogleHomeTargetConfigurationViewModel.State.Loaded -> {
                binding.settingsBaseLoading.isVisible = false
                binding.settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), binding.settingsBaseRecyclerView)
            }
        }
    }

    private fun GoogleHomeTargetConfigurationViewModel.State.Loaded.loadItems(): List<BaseSettingsItem> {
        return listOf(
            GenericSettingsItem.Setting(
                getString(R.string.shared_configuration_choose_items_title),
                getString(R.string.shared_configuration_choose_items_content),
                icon = null,
                onClick = ::openReconfigure
            ),
            GenericSettingsItem.Dropdown(
                getString(R.string.shared_configuration_hide_mode_title),
                getString(R.string.shared_configuration_hide_mode_content, getString(hideMode.label)),
                null,
                hideMode,
                onSet = viewModel::onHideModeChanged,
                HideMode.entries
            ) {
                it.label
            }
        )
    }

    private fun openReconfigure() {
        val intentSender = SmartspacerWidgetProvider.getReconfigureIntentSender(
            requireContext(),
            id ?: return
        )
        val options = ActivityOptions.makeBasic().apply {
            if (Build.VERSION.SDK_INT >= 34) {
                pendingIntentBackgroundActivityStartMode =
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
                pendingIntentCreatorBackgroundActivityStartMode =
                    ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
            }
        }
        requireActivity().startIntentSender(
            intentSender,
            null,
            0,
            0,
            0,
            options.toBundle()
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}