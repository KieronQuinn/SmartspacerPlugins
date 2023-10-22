package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.carousel

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
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Header
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesBack
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.shared.utils.onClicked
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate.Carousel.CarouselItem
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.base.BaseSettingsFabFragment
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.carousel.TargetCarouselViewModel.State
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.icon.IconPickerFragment.Companion.setupIconResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.tapaction.TapActionFragment.Companion.setupTapActionResultListener
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.text.TextInputFragment.Companion.setupTextResultListener
import com.kieronquinn.app.smartspacer.sdk.utils.getParcelableArrayListCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TargetCarouselFragment: BaseSettingsFabFragment(), BackAvailable, ProvidesBack {

    companion object {
        private const val KEY_RESULT = "result"

        fun Fragment.setupCarouselItemResultListener(
            key: String,
            callback: (result: List<CarouselItem>) -> Unit
        ) {
            setFragmentResultListener(key) { requestKey, bundle ->
                if(requestKey != key) return@setFragmentResultListener
                val result = bundle.getParcelableArrayListCompat(
                    KEY_RESULT, CarouselItem::class.java
                ) ?: return@setFragmentResultListener
                callback.invoke(result)
            }
        }

        private const val KEY_UPPER_TEXT = "carousel_upper_text_"
        private const val KEY_ICON = "carousel_icon_"
        private const val KEY_LOWER_TEXT = "carousel_lower_text_"
        private const val KEY_TAP_ACTION = "carousel_tap_action_"
    }

    private val viewModel by viewModel<TargetCarouselViewModel>()
    private val args by navArgs<TargetCarouselFragmentArgs>()

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
        setupState()
        setupListeners()
        viewModel.setup(config.current)
    }

    override fun onBackPressed(): Boolean {
        val current = (viewModel.state.value as? State.Loaded)?.items
        if(current != null){
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
        for(i in 0 until TargetCarouselViewModelImpl.MAX_ITEMS) {
            setupTextResultListener(KEY_UPPER_TEXT + i) {
                viewModel.onUpperTextChanged(i, it)
            }
            setupIconResultListener(KEY_ICON + i) {
                viewModel.onIconChanged(i, it)
            }
            setupTextResultListener(KEY_LOWER_TEXT + i) {
                viewModel.onLowerTextChanged(i, it)
            }
            setupTapActionResultListener(KEY_TAP_ACTION + i) {
                viewModel.onTapActionChanged(i, it)
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
        return items.mapIndexed { index, carouselItem ->
            carouselItem.loadItem(index)
        }.flatten()
    }

    private fun CarouselItem.loadItem(index: Int): List<BaseSettingsItem> {
        return listOf(
            Header(getString(R.string.configuration_carousel_item_header, index + 1)),
            Setting(
                getString(R.string.configuration_carousel_item_upper_title),
                upperText.describe(),
                icon = null
            ) {
                viewModel.onUpperTextClicked(KEY_UPPER_TEXT + index, upperText)
            },
            Setting(
                getString(R.string.configuration_carousel_item_icon_title),
                image.describe(requireContext()),
                icon = null
            ) {
                viewModel.onIconClicked(KEY_ICON + index, image)
            },
            Setting(
                getString(R.string.configuration_carousel_item_lower_title),
                lowerText.describe(),
                icon = null
            ) {
                viewModel.onLowerTextClicked(KEY_LOWER_TEXT + index, lowerText)
            },
            Setting(
                getString(R.string.configuration_carousel_item_tap_action),
                tapAction.describe(requireContext()),
                icon = null
            ) {
                viewModel.onTapActionClicked(KEY_TAP_ACTION + index, tapAction)
            },
            Setting(
                getString(R.string.configuration_carousel_item_delete),
                "",
                icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
            ) {
                viewModel.onDeleteClicked(index)
            }
        )
    }

    @Parcelize
    data class Config(
        val key: String,
        val current: List<CarouselItem>
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList()) {
        init {
            //Disable animations due to shared titles
            setHasStableIds(false)
        }
    }

}