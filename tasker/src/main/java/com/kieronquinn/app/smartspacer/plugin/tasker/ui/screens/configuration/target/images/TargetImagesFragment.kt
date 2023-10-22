package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.images

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
import com.kieronquinn.app.smartspacer.plugin.tasker.model.Icon
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsFabFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsWithActionAdapter
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsWithActionViewModel.SettingsWithActionItem.SettingWithAction
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.images.TargetImagesViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment.Companion.setupIconResultListener
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableArrayListCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TargetImagesFragment: BaseSettingsFabFragment(), BackAvailable, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"
        private const val KEY_LAST_CLICKED_INDEX = "last_clicked_index"
        private const val REQUEST_KEY_ICON = "images_icon"

        fun Fragment.setupImagesResultListener(key: String, callback: (result: List<Icon>) -> Unit) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableArrayListCompat(KEY_RESULT, Icon::class.java)
                    ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }
    }

    private val viewModel by viewModel<TargetImagesViewModel>()
    private val args by navArgs<TargetImagesFragmentArgs>()

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
        setupListener()
        setupFab()
        setupState()
        viewModel.setup(config.icons)
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

    private fun setupListener() {
        setupIconResultListener(REQUEST_KEY_ICON) {
            val index = arguments?.getInt(KEY_LAST_CLICKED_INDEX, -1)
                ?.takeIf { index -> index != -1 } ?: return@setupIconResultListener
            viewModel.onItemChanged(index, it)
        }
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
                settingsBaseFab.isVisible = true
                adapter.update(state.loadItems(), settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> {
        val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        return items.mapIndexed { index, icon ->
            SettingWithAction(
                getString(R.string.configuration_images_image_title, index + 1),
                icon.describe(requireContext()),
                icon = null,
                actionIcon = deleteIcon,
                onActionClicked = { viewModel.onItemDeleteClicked(index) }
            ) {
                arguments = Bundle(arguments).apply {
                    putInt(KEY_LAST_CLICKED_INDEX, index)
                }
                viewModel.onItemClicked(REQUEST_KEY_ICON, icon)
            }
        }
    }

    @Parcelize
    data class Config(
        val key: String,
        val icons: List<Icon>
    ): Parcelable

    inner class Adapter: BaseSettingsWithActionAdapter(binding.settingsBaseRecyclerView, emptyList())

}