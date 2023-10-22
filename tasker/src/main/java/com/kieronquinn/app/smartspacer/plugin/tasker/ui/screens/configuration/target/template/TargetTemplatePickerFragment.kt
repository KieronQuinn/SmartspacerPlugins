package com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.template

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
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
import com.kieronquinn.app.smartspacer.plugin.tasker.R
import com.kieronquinn.app.smartspacer.plugin.tasker.model.TargetTemplate
import com.kieronquinn.app.smartspacer.plugin.tasker.ui.screens.configuration.target.template.TargetTemplatePickerViewModel.State
import com.kieronquinn.app.smartspacer.sdk.utils.getSerializableCompat
import kotlinx.parcelize.Parcelize
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class TargetTemplatePickerFragment: BaseSettingsFragment(), BackAvailable {

    companion object {
        private const val KEY_TARGET_TEMPLATE_PICKER = "target_template_picker"
        private const val KEY_TYPE = "type"

        private val TARGETS = listOf(
            Type(
                R.string.configuration_target_basic_title,
                R.string.configuration_target_basic_content,
                TargetTemplate.Basic::class.java
            ),
            Type(
                R.string.configuration_target_button_title,
                R.string.configuration_target_button_content,
                TargetTemplate.Button::class.java
            ),
            Type(
                R.string.configuration_carousel_title,
                R.string.configuration_carousel_content,
                TargetTemplate.Carousel::class.java
            ),
            Type(
                R.string.configuration_doorbell_title,
                R.string.configuration_doorbell_content,
                TargetTemplate.Doorbell::class.java
            ),
            Type(
                R.string.configuration_target_head_to_head_title,
                R.string.configuration_target_head_to_head_content,
                TargetTemplate.HeadToHead::class.java
            ),
            Type(
                R.string.configuration_image_title,
                R.string.configuration_image_content,
                TargetTemplate.Image::class.java
            ),
            Type(
                R.string.configuration_images_title,
                R.string.configuration_images_content,
                TargetTemplate.Images::class.java
            ),
            Type(
                R.string.configuration_target_list_title,
                R.string.configuration_target_list_content,
                TargetTemplate.ListItems::class.java
            ),
            Type(
                R.string.configuration_loyalty_card_title,
                R.string.configuration_loyalty_card_content,
                TargetTemplate.LoyaltyCard::class.java
            )
        )

        fun Fragment.setupTargetTemplatePickerListener(
            callback: (type: Class<out TargetTemplate>) -> Unit
        ) {
            setFragmentResultListener(KEY_TARGET_TEMPLATE_PICKER) { requestKey, bundle ->
                if(requestKey != KEY_TARGET_TEMPLATE_PICKER) return@setFragmentResultListener
                val type = bundle.getSerializableCompat(KEY_TYPE, Class::class.java)
                    as? Class<out TargetTemplate>
                    ?: return@setFragmentResultListener
                callback.invoke(type)
            }
        }
    }

    private val viewModel by viewModel<TargetTemplatePickerViewModel>()
    private val args by navArgs<TargetTemplatePickerFragmentArgs>()

    override val adapter by lazy {
        Adapter()
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        viewModel.setup(args.config.current)
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
            State.Loading -> {
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
        return TARGETS.map {
            RadioCard(
                it.clazz == current,
                getString(it.title),
                getString(it.content)
            ){
                onTypeClicked(it.clazz)
            }
        }.sortedBy {
            it.title.toString().lowercase()
        }
    }

    private fun onTypeClicked(type: Class<out TargetTemplate>) {
        setFragmentResult(KEY_TARGET_TEMPLATE_PICKER, bundleOf(
            KEY_TYPE to type
        ))
        viewModel.dismiss()
    }

    data class Type(
        @StringRes
        val title: Int,
        @StringRes
        val content: Int,
        val clazz: Class<out TargetTemplate>
    )

    @Parcelize
    data class Config(
        val current: Class<out TargetTemplate>
    ): Parcelable

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}