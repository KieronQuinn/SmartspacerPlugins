package com.kieronquinn.app.smartspacer.plugin.googlekeep.ui.screens.configuration

import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.googlekeep.R
import com.kieronquinn.app.smartspacer.plugin.googlekeep.model.Note
import com.kieronquinn.app.smartspacer.plugin.googlekeep.ui.screens.configuration.ConfigurationViewModel.State
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

class ConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    private val noteSelectLauncher = registerForActivityResult(StartIntentSenderForResult()) {
        //No-op
    }

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
            Setting(
                getString(R.string.target_configuration_select_note_title),
                data.note?.title
                    ?: getString(R.string.target_configuration_select_note_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_keep)
            ) {
                viewModel.onSelectNoteClicked(requireContext(), noteSelectLauncher)
            },
            SwitchSetting(
                data.showIndented,
                getString(R.string.target_configuration_show_indented_title),
                getString(R.string.target_configuration_show_indented_content),
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_configuration_show_indented
                ),
                onChanged = viewModel::onShowIndentedChanged
            ).takeIf { data.note is Note.ListNote },
            SwitchSetting(
                data.hideIfEmpty,
                getString(R.string.target_configuration_hide_if_empty_title),
                getString(R.string.target_configuration_hide_if_empty_content),
                ContextCompat.getDrawable(
                    requireContext(), R.drawable.ic_configuration_hide_if_empty
                ),
                onChanged = viewModel::onHideIfEmptyChanged
            ).takeIf { data.note is Note.ListNote }
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}