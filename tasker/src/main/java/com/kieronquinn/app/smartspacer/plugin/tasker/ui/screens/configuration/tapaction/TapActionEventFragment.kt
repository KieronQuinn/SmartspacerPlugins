package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.tapaction

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfo
import com.joaomgcd.taskerpluginlibrary.input.TaskerInputInfos
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.events.TapActionEvent
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Manipulative.Companion.getVariablesFromString
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerTapActionEventInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.TapActionEventActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.tapaction.TapActionEventViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.Companion.setupStringResultListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TapActionEventFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    companion object {
        const val REQUEST_KEY_ID = "tap_action_event_id"
    }

    private val viewModel by viewModel<TapActionEventViewModel>()

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val configurationActivity by lazy {
        requireActivity() as TapActionEventActivity
    }

    private val taskerAction by lazy {
        TapActionEvent(configurationActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerAction.onCreate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupState()
        viewModel.setup(configurationActivity.taskerInput ?: SmartspacerTapActionEventInput())
    }

    override fun onBackPressed(): Boolean {
        val current = viewModel.state.value as? State.Loaded ?: return false
        val variables = current.id?.getVariablesFromString()?.map {
            TaskerInputInfo(it, it, null, true, it)
        }?.let {
            TaskerInputInfos().apply {
                addAll(it)
            }
        } ?: TaskerInputInfos()
        configurationActivity.setDynamicInputs(variables)
        configurationActivity.setStaticInputs(SmartspacerTapActionEventInput(current.id))
        taskerAction.finishForTasker()
        return false
    }

    private fun setupListener() {
        setupStringResultListener(REQUEST_KEY_ID) {
            viewModel.onIdChanged(it)
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
        return listOf(
            Setting(
                getString(R.string.tap_action_event_id_title),
                id ?: getString(R.string.tap_action_event_id_content),
                icon = null,
                onClick = viewModel::onIdClicked
            )
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}