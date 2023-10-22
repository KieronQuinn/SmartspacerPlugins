package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.visibility.complication

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
import com.kieronquinn.app.smartspacer.plugin.tasker.actions.SetComplicationVisibilityAction
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerComplicationVisibilityTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.ComplicationVisibilityActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.complication.picker.ComplicationPickerFragment.Companion.setupComplicationPickerListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.visibility.complication.ComplicationVisibilityViewModel.State
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class ComplicationVisibilityFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    private val viewModel by viewModel<ComplicationVisibilityViewModel>()

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val configurationActivity by lazy {
        requireActivity() as ComplicationVisibilityActivity
    }

    private val taskerAction by lazy {
        SetComplicationVisibilityAction(configurationActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerAction.onCreate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupState()
        viewModel.setup(configurationActivity.taskerInput ?: SmartspacerComplicationVisibilityTaskerInput())
    }

    override fun onBackPressed(): Boolean {
        val state = (viewModel.state.value as? State.Loaded) ?: return false
        val databaseComplication = state.complication ?: return false
        configurationActivity.setStaticInputs(
            SmartspacerComplicationVisibilityTaskerInput(
                databaseComplication.smartspacerId,
                databaseComplication.name,
                state.visible
            )
        )
        taskerAction.finishForTasker()
        return true
    }

    private fun setupListener() {
        setupComplicationPickerListener {
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
                getString(R.string.configuration_complication_visibility_complication_title),
                complication?.name
                    ?: getString(R.string.configuration_complication_visibility_complication_content),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_tasker),
                onClick = viewModel::onSmartspacerComplicationClicked
            ),
            SwitchSetting(
                visible,
                getString(R.string.configuration_complication_visibility_visibility_title),
                getString(R.string.configuration_complication_visibility_visibility_content),
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_visibility),
                onChanged = viewModel::setVisibility
            ).takeIf { complication != null }
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}