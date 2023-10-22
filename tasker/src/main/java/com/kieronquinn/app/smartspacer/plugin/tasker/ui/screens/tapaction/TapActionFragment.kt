package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.fragment.navArgs
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TapAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.app.AppPickerFragment.Companion.setupAppResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.string.StringInputFragment.Companion.setupStringResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionViewModel.State
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TapActionFragment: BaseSettingsFragment(), BackAvailable, ProvidesTitle {

    companion object {
        private const val KEY_RESULT = "result"
        const val REQUEST_KEY_LAUNCH_APP = "launch_app"
        const val REQUEST_KEY_URL = "tap_action_url"
        const val REQUEST_KEY_TASKER_ID = "tasker_id"

        fun Fragment.setupTapActionResultListener(
            key: String,
            callback: (result: TapAction) -> Unit
        ) {
            setupTapActionResultListenerNullable(key) {
                callback.invoke(it ?: return@setupTapActionResultListenerNullable)
            }
        }

        fun Fragment.setupTapActionResultListenerNullable(
            key: String,
            callback: (result: TapAction?) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableCompat(KEY_RESULT, TapAction::class.java)
                callback.invoke(result)
            }
        }
    }

    private val args by navArgs<TapActionFragmentArgs>()
    private val viewModel by viewModel<TapActionViewModel>()

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

    override fun getTitle(): CharSequence {
        return getString(config.title)
    }

    private fun setupListeners() {
        setupStringResultListener(REQUEST_KEY_URL) {
            dismissWithResult(TapAction.Url(it))
        }
        setupStringResultListener(REQUEST_KEY_TASKER_ID) {
            dismissWithResult(TapAction.TaskerEvent(it))
        }
        setupAppResultListener(REQUEST_KEY_LAUNCH_APP) { packageName, label ->
            dismissWithResult(TapAction.LaunchApp(packageName, label.toString()))
        }
    }

    private fun dismissWithResult(tapAction: TapAction?) {
        whenResumed {
            setFragmentResult(config.key, bundleOf(
                KEY_RESULT to tapAction
            ))
            viewModel.dismiss()
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
                getString(R.string.tap_action_use_parent_title),
                getString(R.string.tap_action_use_parent_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear)
            ) {
                dismissWithResult(null)
            }.takeIf { config.showParent },
            Setting(
                getString(R.string.tap_action_none_title),
                getString(R.string.tap_action_none_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_clear)
            ) {
                dismissWithResult(null)
            }.takeIf { config.showNone },
            Setting(
                getString(R.string.tap_action_launch_app_title),
                getString(R.string.tap_action_launch_app_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_icon_built_in)
            ) {
                viewModel.onLaunchAppClicked(REQUEST_KEY_LAUNCH_APP)
            },
            Setting(
                getString(R.string.tap_action_open_url_title),
                getString(R.string.tap_action_open_url_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_web)
            ) {
                viewModel.onOpenUrlClicked(REQUEST_KEY_URL, tapAction as? TapAction.Url)
            },
            Setting(
                getString(R.string.tap_action_trigger_tasker_event_title),
                getString(R.string.tap_action_trigger_tasker_event_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_tasker)
            ) {
                viewModel.onTaskerEventClicked(
                    REQUEST_KEY_TASKER_ID,
                    tapAction as? TapAction.TaskerEvent
                )
            }
        )
    }

    @Parcelize
    data class Config(
        @StringRes
        val title: Int,
        val key: String,
        val current: TapAction?,
        val showParent: Boolean = false,
        val showNone: Boolean = false
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}