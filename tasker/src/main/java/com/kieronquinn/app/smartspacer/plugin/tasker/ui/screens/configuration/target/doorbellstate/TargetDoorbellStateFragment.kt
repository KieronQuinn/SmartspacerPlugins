package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.doorbellstate

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.RadioCard
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Doorbell.DoorbellState
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Doorbell.DoorbellState.DoorbellStateType
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.doorbellstate.TargetDoorbellStateViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.options.Doorbell
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TargetDoorbellStateFragment: BaseSettingsFragment(), BackAvailable {

    companion object {
        private const val KEY_RESULT = "result"

        fun Fragment.setupDoorbellStateResultListener(
            key: String,
            callback: (result: DoorbellState) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableCompat(KEY_RESULT, DoorbellState::class.java)
                    ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<TargetDoorbellStateViewModel>()
    private val args by navArgs<TargetDoorbellStateFragmentArgs>()

    private val config by lazy {
        args.config as Config
    }

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        viewModel.setup(config.current)
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
        return doorbellStates.entries.map {
            RadioCard(
                it.key == selected,
                it.value.getName(requireContext()),
                it.value.getDescription(requireContext())
            ) {
                onStateSelected(it.value)
            }
        }
    }

    private fun onStateSelected(provider: Doorbell.DoorbellOptionsProvider<*>) {
        setFragmentResult(config.key, bundleOf(
            KEY_RESULT to provider.createBlank(requireContext())
        ))
        viewModel.dismiss()
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: DoorbellStateType
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}