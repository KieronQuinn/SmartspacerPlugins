package com.kieronquinn.app.smartspacer.plugin.aftership.ui.screens.configuration

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.aftership.R
import com.kieronquinn.app.smartspacer.plugin.aftership.ui.screens.configuration.AftershipTargetConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class AftershipTargetConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    private val viewModel by viewModel<AftershipTargetConfigurationViewModel>()

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
        return listOf(
            SwitchSetting(
                targetData.showImage,
                getString(R.string.target_configuration_show_image_title),
                getString(R.string.target_configuration_show_image_content),
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_target_configuration_show_image
                ),
                onChanged = viewModel::onShowImageChanged
            ),
            SwitchSetting(
                targetData.showMap,
                getString(R.string.target_configuration_show_map_title),
                getText(R.string.target_configuration_show_map_content),
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_target_configuration_show_map
                ),
                onChanged = viewModel::onShowMapChanged
            ),
            SwitchSetting(
                targetData.enableUpdates,
                getString(R.string.target_configuration_enable_updates_title),
                getText(R.string.target_configuration_enable_updates_content),
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_target_configuration_refresh
                ),
                onChanged = viewModel::onEnableUpdatesChanged
            ),
            Setting(
                getString(R.string.target_configuration_reset_dismissed_title),
                getString(R.string.target_configuration_reset_dismissed_content),
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_target_configuration_reset_dismissed
                ),
                onClick = viewModel::onResetClearedClicked
            )
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}