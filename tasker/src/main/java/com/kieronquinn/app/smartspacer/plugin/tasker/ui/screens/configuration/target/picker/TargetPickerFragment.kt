package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.picker

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.database.Target
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.picker.TargetPickerViewModel.State
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TargetPickerFragment: BaseSettingsFragment(), BackAvailable {

    companion object {
        private const val KEY_TARGET_PICKER = "target_picker"
        private const val KEY_SMARTSPACER_ID = "smartspacer_id"

        fun Fragment.setupTargetPickerListener(callback: (smartspacerId: String) -> Unit) {
            setFragmentResultListener(KEY_TARGET_PICKER) { requestKey, bundle ->
                if(requestKey != KEY_TARGET_PICKER) return@setFragmentResultListener
                val smartspacerId = bundle.getString(KEY_SMARTSPACER_ID)
                    ?: return@setFragmentResultListener
                callback.invoke(smartspacerId)
            }
        }
    }

    private val viewModel by viewModel<TargetPickerViewModel>()

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override val adapter by lazy {
        Adapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

    private fun handleState(state: State) = with(binding) {
        when(state) {
            is State.Loading -> {
                settingsBaseLoading.isVisible = true
                settingsBaseRecyclerView.isVisible = false
            }
            is State.Loaded -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), binding.settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return targets.map {
            Setting(
                it.name,
                getString(R.string.configuration_target_select_subtitle, it.smartspacerId),
                icon = null
            ) {
                onTargetClicked(it)
            }
        }.ifEmpty {
            listOf(
                Card(
                    ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
                    getString(R.string.configuration_target_select_empty)
                )
            )
        }
    }

    private fun onTargetClicked(target: Target) {
        setFragmentResult(
            KEY_TARGET_PICKER,
            bundleOf(KEY_SMARTSPACER_ID to target.smartspacerId)
        )
        viewModel.dismiss()
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}