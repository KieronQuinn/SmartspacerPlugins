package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState.Shortcuts
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsFabFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsWithActionAdapter
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsWithActionViewModel.SettingsWithActionItem.SettingWithAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.TargetExpandedShortcutsViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.shortcut.TargetExpandedShortcutFragment.Companion.setupExpandedShortcutResultListener
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TargetExpandedShortcutsFragment: BaseSettingsFabFragment(), BackAvailable, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"
        const val REQUEST_KEY_SHORTCUT = "expanded_shortcut_shortcut_"

        fun Fragment.setupExpandedShortcutsResultListener(
            key: String,
            callback: (result: Shortcuts) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableCompat(KEY_RESULT, Shortcuts::class.java)
                    ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<TargetExpandedShortcutsViewModel>()
    private val args by navArgs<TargetExpandedShortcutsFragmentArgs>()

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
        setupFab()
        setupListeners()
        setupState()
        viewModel.setup(config.current)
    }

    override fun onBackPressed(): Boolean {
        val current = (viewModel.state.value as? State.Loaded)?.shortcuts
        if(current != null){
            setFragmentResult(config.key, bundleOf(
                KEY_RESULT to current
            ))
        }
        viewModel.dismiss()
        return true
    }

    private fun setupFab() = with(binding.settingsBaseFab) {
        text = getString(R.string.add)
        icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_add)
        whenResumed {
            onClicked().collect {
                viewModel.onAddClicked(requireContext())
            }
        }
    }

    private fun setupListeners() {
        for(i in 0 until TargetExpandedShortcutsViewModelImpl.MAX_ITEMS) {
            setupExpandedShortcutResultListener(REQUEST_KEY_SHORTCUT + i) {
                viewModel.onShortcutChanged(i, it)
            }
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
                settingsBaseFab.isVisible = false
            }
            is State.Loaded -> {
                settingsBaseLoading.isVisible = false
                settingsBaseRecyclerView.isVisible = true
                settingsBaseFab.isVisible = state.showFab
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        return shortcuts.shortcuts.mapIndexed { index, shortcut ->
            SettingWithAction(
                shortcut.label.describe(),
                shortcut.tapAction.describe(requireContext()),
                icon = null,
                actionIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete),
                onActionClicked = { viewModel.onShortcutDeleted(index) }
            ) {
                viewModel.onShortcutClicked(REQUEST_KEY_SHORTCUT + index, shortcut)
            }
        }
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: Shortcuts
    ): Parcelable

    inner class Adapter: BaseSettingsWithActionAdapter(binding.settingsBaseRecyclerView, emptyList()) {
        init {
            //Disable animations due to potentially shared titles
            setHasStableIds(false)
        }
    }

}