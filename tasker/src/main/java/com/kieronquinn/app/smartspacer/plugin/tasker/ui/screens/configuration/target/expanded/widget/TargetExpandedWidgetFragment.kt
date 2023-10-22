package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget

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
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Switch
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.TargetExtras.ExpandedState.Widget
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.TargetExpandedWidgetViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.expanded.widget.picker.TargetExpandedWidgetPickerFragment.Companion.setupExpandedWidgetPickerResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.Companion.setupStringResultListener
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel

class TargetExpandedWidgetFragment: BaseSettingsFragment(), BackAvailable, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"
        const val REQUEST_KEY_WIDGET = "expanded_widget_widget"
        const val REQUEST_KEY_ID = "expanded_widget_id"
        const val REQUEST_KEY_WIDTH = "expanded_widget_width"
        const val REQUEST_KEY_HEIGHT = "expanded_widget_height"

        fun Fragment.setupExpandedWidgetResultListener(
            key: String,
            callback: (result: Widget?) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableCompat(KEY_RESULT, Widget::class.java)
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<TargetExpandedWidgetViewModel>()
    private val navArgs by navArgs<TargetExpandedWidgetFragmentArgs>()

    private val config by lazy {
        navArgs.config as Config
    }

    override val adapter by lazy {
        Adapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        setupListeners()
        viewModel.setup(config.current)
    }

    override fun onBackPressed(): Boolean {
        val current = viewModel.state.value as? State.Loaded
        if(current != null) {
            setFragmentResult(config.key, bundleOf(
                KEY_RESULT to current.widget?.takeIf { it._componentName != null }
            ))
        }
        viewModel.dismiss()
        return true
    }

    private fun setupListeners() {
        setupExpandedWidgetPickerResultListener(REQUEST_KEY_WIDGET) { componentName, label ->
            viewModel.onComponentNameChanged(componentName, label)
        }
        setupStringResultListener(REQUEST_KEY_ID) {
            viewModel.onIdChanged(it)
        }
        setupStringResultListener(REQUEST_KEY_WIDTH) {
            viewModel.onWidthChanged(it)
        }
        setupStringResultListener(REQUEST_KEY_HEIGHT) {
            viewModel.onHeightChanged(it)
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
                adapter.update(state.getItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.getItems(): List<BaseSettingsItem> {
        val items = if(widget != null) {
            listOf(
                Setting(
                    getString(R.string.configuration_target_expanded_widget_component_title),
                    widget.label
                        ?: getString(R.string.configuration_target_expanded_widget_component_content),
                    icon = null,
                    onClick = viewModel::onComponentNameClicked
                ),
                Setting(
                    getString(R.string.configuration_target_expanded_widget_id_title),
                    widget.id
                        ?: getText(R.string.configuration_target_expanded_widget_id_content),
                    icon = null,
                    onClick = viewModel::onIdClicked
                ),
                SwitchSetting(
                    widget.showWhenLocked,
                    getString(R.string.configuration_target_expanded_widget_show_when_locked_title),
                    getString(R.string.configuration_target_expanded_widget_show_when_locked_content),
                    icon = null,
                    onChanged = viewModel::onShowWhenLockedChanged
                ),
                SwitchSetting(
                    widget.skipConfigure,
                    getString(R.string.configuration_target_expanded_widget_skip_configuration_title),
                    getText(R.string.configuration_target_expanded_widget_skip_configuration_content),
                    icon = null,
                    onChanged = viewModel::onSkipConfigurationChanged
                ),
                Setting(
                    getString(R.string.configuration_target_expanded_widget_width_title),
                    widget.getWidthContent(),
                    icon = null,
                    isEnabled = widget.id != null,
                    onClick = viewModel::onWidthClicked
                ),
                Setting(
                    getString(R.string.configuration_target_expanded_widget_height_title),
                    widget.getHeightContent(),
                    icon = null,
                    isEnabled = widget.id != null,
                    onClick = viewModel::onHeightClicked
                )
            )
        }else emptyList()
        return listOf(
            Switch(
                widget != null,
                getString(R.string.configuration_target_expanded_widget_component_switch),
                viewModel::onEnabledChanged
            )
        ) + items
    }

    private fun Widget.getWidthContent(): CharSequence {
        if(id == null) return getString(R.string.configuration_target_expanded_widget_width_disabled)
        return _width?.let { "$it ${getString(R.string.input_string_suffix_pixels)}" }
            ?: getText(R.string.configuration_target_expanded_widget_width_content)
    }

    private fun Widget.getHeightContent(): CharSequence {
        if(id == null) return getString(R.string.configuration_target_expanded_widget_height_disabled)
        return _height?.let { "$it ${getString(R.string.input_string_suffix_pixels)}" }
            ?: getText(R.string.configuration_target_expanded_widget_height_content)
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: Widget?
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}