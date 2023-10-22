package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.dismiss

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.events.TargetDismissEvent
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTargetDismissInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.TargetDismissEventActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.dismiss.TargetDismissEventConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.picker.TargetPickerFragment.Companion.setupTargetPickerListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TargetDismissEventConfigurationFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    private val viewModel by viewModel<TargetDismissEventConfigurationViewModel>()

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val configurationActivity by lazy {
        requireActivity() as TargetDismissEventActivity
    }

    private val taskerAction by lazy {
        TargetDismissEvent(configurationActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerAction.onCreate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupState()
        viewModel.setup(configurationActivity.taskerInput ?: SmartspacerTargetDismissInput())
    }

    override fun onBackPressed(): Boolean {
        val current = viewModel.state.value as? State.Loaded ?: return false
        configurationActivity.setStaticInputs(
            SmartspacerTargetDismissInput(
                current.target?.smartspacerId,
                current.target?.name
            )
        )
        taskerAction.finishForTasker()
        return false
    }

    private fun setupListeners() {
        setupTargetPickerListener {
            viewModel.onSmartspacerIdChanged(it)
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
            GenericSettingsItem.Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning),
                getString(R.string.dismiss_target_event_target_not_sending)
            ).takeIf { target?.current?.targetExtras?.canBeDismissed == false },
            Setting(
                getString(R.string.dismiss_target_event_target_title),
                target?.name ?: getString(R.string.dismiss_target_event_target_content),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_tasker),
                onClick = viewModel::onTargetClicked
            )
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}