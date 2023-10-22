package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.listitems

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
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Text
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsFabFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsWithActionAdapter
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsWithActionViewModel.SettingsWithActionItem.SettingWithAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.listitems.TargetListItemsViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputFragment.Companion.setupTextResultListener
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableArrayListCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TargetListItemsFragment: BaseSettingsFabFragment(), BackAvailable, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"
        private const val REQUEST_KEY_TEXT = "list_items_text_"

        fun Fragment.setupListItemsResultListener(
            key: String,
            callback: (result: List<Text>) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableArrayListCompat(KEY_RESULT, Text::class.java)
                    ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<TargetListItemsViewModel>()
    private val args by navArgs<TargetListItemsFragmentArgs>()

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
        val current = (viewModel.state.value as? State.Loaded)?.items
        if(current != null) {
            setFragmentResult(config.key, bundleOf(
                KEY_RESULT to ArrayList(current)
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
        for(i in 0 until TargetListItemsViewModelImpl.MAX_ITEMS) {
            setupTextResultListener(REQUEST_KEY_TEXT + i) {
                viewModel.onItemChanged(i, it)
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
        return items.mapIndexed { index, text ->
            SettingWithAction(
                getString(
                    R.string.configuration_target_list_items_content_default, index + 1
                ),
                text.describe(),
                icon = null,
                actionIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete),
                onActionClicked = { viewModel.onItemDeleted(index) }
            ) {
                viewModel.onItemClicked(REQUEST_KEY_TEXT + index, text)
            }
        }
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: List<Text>
    ): Parcelable

    inner class Adapter: BaseSettingsWithActionAdapter(binding.settingsBaseRecyclerView, emptyList())

}