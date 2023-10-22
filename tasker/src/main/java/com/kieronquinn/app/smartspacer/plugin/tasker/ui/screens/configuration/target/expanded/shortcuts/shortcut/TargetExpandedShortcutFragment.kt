package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.shortcut

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
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState.Shortcuts.Shortcut
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.shortcut.TargetExpandedShortcutViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment.Companion.setupIconResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionFragment.Companion.setupTapActionResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputFragment.Companion.setupTextResultListener
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TargetExpandedShortcutFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"
        const val REQUEST_KEY_TITLE = "expanded_shortcut_title"
        const val REQUEST_KEY_ICON = "expanded_shortcut_icon"
        const val REQUEST_KEY_TAP_ACTION = "expanded_shortcut_tap_action"

        fun Fragment.setupExpandedShortcutResultListener(
            key: String,
            callback: (result: Shortcut) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableCompat(KEY_RESULT, Shortcut::class.java)
                    ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<TargetExpandedShortcutViewModel>()
    private val args by navArgs<TargetExpandedShortcutFragmentArgs>()

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
        setupListeners()
        setupState()
        viewModel.setup(config.current)
    }

    override fun onBackPressed(): Boolean {
        val current = (viewModel.state.value as? State.Loaded)?.shortcut
        if(current != null) {
            setFragmentResult(config.key, bundleOf(
                KEY_RESULT to current
            ))
        }
        viewModel.dismiss()
        return true
    }

    private fun setupListeners() {
        setupTextResultListener(REQUEST_KEY_TITLE) {
            viewModel.onTitleChanged(it)
        }
        setupIconResultListener(REQUEST_KEY_ICON) {
            viewModel.onIconChanged(it)
        }
        setupTapActionResultListener(REQUEST_KEY_TAP_ACTION) {
            viewModel.onTapActionChanged(it)
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
                getString(R.string.configuration_target_expanded_shortcuts_shortcut_title_title),
                shortcut.label.describe(),
                icon = null,
                onClick = viewModel::onTitleClicked
            ),
            Setting(
                getString(R.string.configuration_target_expanded_shortcuts_shortcut_icon_title),
                shortcut.icon.describe(requireContext()),
                icon = null,
                onClick = viewModel::onIconClicked
            ),
            Setting(
                getString(R.string.configuration_target_expanded_shortcuts_shortcut_tap_action_title),
                shortcut.tapAction.describe(requireContext()),
                icon = null,
                onClick = viewModel::onTapActionClicked
            ),
            SwitchSetting(
                shortcut.showWhenLocked,
                getString(R.string.configuration_target_expanded_shortcuts_shortcut_show_when_locked_title),
                getString(R.string.configuration_target_expanded_shortcuts_shortcut_show_when_locked_content),
                icon = null,
                onChanged = viewModel::onShowWhenLockedChanged
            )
        )
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: Shortcut
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}