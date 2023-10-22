package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.requirement

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.actions.SetRequirementAction
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerRequirementSetTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.RequirementUpdateActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.requirement.RequirementUpdateViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.requirement.picker.RequirementPickerFragment.Companion.setupRequirementPickerListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class RequirementUpdateFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    private val viewModel by viewModel<RequirementUpdateViewModel>()

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val configurationActivity by lazy {
        requireActivity() as RequirementUpdateActivity
    }

    private val taskerAction by lazy {
        SetRequirementAction(configurationActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerAction.onCreate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupState()
        viewModel.setup(configurationActivity.taskerInput ?: SmartspacerRequirementSetTaskerInput())
    }

    override fun onBackPressed(): Boolean {
        val state = (viewModel.state.value as? State.Loaded) ?: return false
        val databaseRequirement = state.requirement ?: return false
        configurationActivity.setStaticInputs(
            SmartspacerRequirementSetTaskerInput(
                databaseRequirement.smartspacerId,
                databaseRequirement.name,
                state.isMet
            )
        )
        taskerAction.finishForTasker()
        return true
    }

    private fun setupListener() {
        setupRequirementPickerListener {
            viewModel.setSmartspacerId(it)
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
                getString(R.string.configuration_requirement_update_requirement_title),
                requirement?.name
                    ?: getString(R.string.configuration_requirement_update_requirement_content),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_tasker),
                onClick = viewModel::onSmartspacerRequirementClicked
            ),
            SwitchSetting(
                isMet,
                getString(R.string.configuration_requirement_update_is_met_title),
                getString(R.string.configuration_requirement_update_is_met_content),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_requirement_met),
                onChanged = viewModel::setUpdate
            ).takeIf { requirement != null }
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}