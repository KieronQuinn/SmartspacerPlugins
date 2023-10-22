package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded

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
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.TargetExpandedViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.appshortcuts.TargetExpandedAppShortcutsFragment.Companion.setupExpandedAppShortcutsResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.shortcuts.TargetExpandedShortcutsFragment.Companion.setupExpandedShortcutsResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.TargetExpandedWidgetFragment.Companion.setupExpandedWidgetResultListener
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TargetExpandedFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"
        const val REQUEST_KEY_WIDGET = "expanded_widget"
        const val REQUEST_KEY_SHORTCUTS = "expanded_shortcuts"
        const val REQUEST_KEY_APP_SHORTCUTS = "expanded_app_shortcuts"

        fun Fragment.setupExpandedResultListener(
            key: String,
            callback: (result: ExpandedState) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableCompat(KEY_RESULT, ExpandedState::class.java)
                    ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<TargetExpandedViewModel>()
    private val args by navArgs<TargetExpandedFragmentArgs>()

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
        setupListeners()
        viewModel.setup(config.current)
    }

    override fun onBackPressed(): Boolean {
        val current = (viewModel.state.value as? State.Loaded)?.state
        setFragmentResult(config.key, bundleOf(
            KEY_RESULT to current
        ))
        viewModel.dismiss()
        return true
    }

    private fun setupListeners() {
        setupExpandedWidgetResultListener(REQUEST_KEY_WIDGET) {
            viewModel.onWidgetChanged(it)
        }
        setupExpandedShortcutsResultListener(REQUEST_KEY_SHORTCUTS) {
            viewModel.onShortcutsChanged(it)
        }
        setupExpandedAppShortcutsResultListener(REQUEST_KEY_APP_SHORTCUTS) {
            viewModel.onAppShortcutsChanged(it)
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
                adapter.update(loadItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun loadItems(): List<BaseSettingsItem> {
        return listOf(
            Setting(
                getString(R.string.configuration_target_expanded_widget_title),
                getString(R.string.configuration_target_expanded_widget_content),
                icon = null,
                onClick = viewModel::onWidgetClicked
            ),
            Setting(
                getString(R.string.configuration_target_expanded_shortcuts_title),
                getString(R.string.configuration_target_expanded_shortcuts_content),
                icon = null,
                onClick = viewModel::onShortcutsClicked
            ),
            Setting(
                getString(R.string.configuration_target_expanded_app_shortcuts_title),
                getString(R.string.configuration_target_expanded_app_shortcuts_content),
                icon = null,
                onClick = viewModel::onAppShortcutsClicked
            )
        )
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: ExpandedState
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}