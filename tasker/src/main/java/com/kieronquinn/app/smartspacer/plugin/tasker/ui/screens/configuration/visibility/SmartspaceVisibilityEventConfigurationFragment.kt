package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.visibility

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.events.SmartspaceVisiblityEvent
import com.kieronquinn.app.smartspacer.plugin.tasker.model.SmartspacerSmartspaceVisibilityTaskerInput
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.activities.SmartspaceVisibilityEventActivity
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.visibility.SmartspaceVisibilityEventConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.visibility.SmartspaceVisibilityEventConfigurationViewModel.Visibility
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class SmartspaceVisibilityEventConfigurationFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    private val viewModel by viewModel<SmartspaceVisibilityEventConfigurationViewModel>()

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    private val configurationActivity by lazy {
        requireActivity() as SmartspaceVisibilityEventActivity
    }

    private val taskerAction by lazy {
        SmartspaceVisiblityEvent(configurationActivity)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        taskerAction.onCreate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        viewModel.setup(configurationActivity.taskerInput?.visibility)
    }

    override fun onBackPressed(): Boolean {
        val current = viewModel.state.value as? State.Loaded ?: return false
        configurationActivity.setStaticInputs(
            SmartspacerSmartspaceVisibilityTaskerInput(current.visibility.toBoolean())
        )
        taskerAction.finishForTasker()
        return false
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
            GenericSettingsItem.Card(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_info),
                getString(R.string.smartspace_visibility_event_title_visibility_warning)
            ),
            GenericSettingsItem.Dropdown(
                getString(R.string.smartspace_visibility_event_title_visibility_title),
                getString(visibility.label),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_visibility),
                visibility,
                viewModel::onVisibilityChanged,
                Visibility.values().toList()
            ) {
                it.label
            }
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}