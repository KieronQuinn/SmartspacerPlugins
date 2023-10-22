package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.appshortcuts

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
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState.AppShortcuts
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app.AppPickerFragment.Companion.setupAppResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.appshortcuts.TargetExpandedAppShortcutsViewModel.State
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel

class TargetExpandedAppShortcutsFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"
        const val REQUEST_KEY_APP = "expanded_app_shortcuts_app"

        fun Fragment.setupExpandedAppShortcutsResultListener(
            key: String,
            callback: (result: AppShortcuts?) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableCompat(KEY_RESULT, AppShortcuts::class.java)
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<TargetExpandedAppShortcutsViewModel>()
    private val args by navArgs<TargetExpandedAppShortcutsFragmentArgs>()

    private val config by lazy {
        args.config as Config
    }

    override val adapter by lazy {
        Adapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListener()
        setupState()
        viewModel.setup(config.current)
    }

    override fun onBackPressed(): Boolean {
        val current = viewModel.state.value as? State.Loaded
        if(current != null){
            setFragmentResult(config.key, bundleOf(
                KEY_RESULT to current.appShortcuts?.takeIf { it.packageName != null }
            ))
        }
        viewModel.dismiss()
        return true
    }

    private fun setupListener() {
        setupAppResultListener(REQUEST_KEY_APP) { packageName, label ->
            viewModel.onAppChanged(packageName, label.toString())
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
        val items = if(appShortcuts != null) {
            listOf(
                GenericSettingsItem.Setting(
                    getString(R.string.configuration_target_expanded_app_shortcuts_app_title),
                    appShortcuts.label
                        ?: getString(R.string.configuration_target_expanded_app_shortcuts_app_content),
                    icon = null,
                    onClick = viewModel::onAppClicked
                ),
                GenericSettingsItem.SwitchSetting(
                    appShortcuts.showWhenLocked,
                    getString(R.string.configuration_target_expanded_app_shortcuts_show_when_locked_title),
                    getString(R.string.configuration_target_expanded_app_shortcuts_show_when_locked_content),
                    icon = null,
                    onChanged = viewModel::onShowWhenLockedChanged
                ),
                GenericSettingsItem.Slider(
                    appShortcuts.appShortcutCount.toFloat(),
                    1f,
                    10f,
                    1f,
                    getString(R.string.configuration_target_expanded_app_shortcuts_count_title),
                    getString(R.string.configuration_target_expanded_app_shortcuts_count_content),
                    icon = null,
                    labelFormatter = { it.toInt().toString() }
                ) {
                    viewModel.onCountChanged(it.toInt())
                }
            )
        }else emptyList()
        return listOf(
            GenericSettingsItem.Switch(
                appShortcuts != null,
                getString(R.string.configuration_target_expanded_app_shortcuts_switch),
                viewModel::onEnabledChanged
            )
        ) + items
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: AppShortcuts?
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}