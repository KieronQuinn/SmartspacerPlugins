package com.kieronquinn.app.smartspacer.plugins.pokemongo.ui.screens.configuration

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.ProvidesTitle
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getSerializableExtraCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugins.pokemongo.PokemonGoPlugin.Variant
import com.kieronquinn.app.smartspacer.plugins.pokemongo.R
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetType
import com.kieronquinn.app.smartspacer.plugins.pokemongo.ui.screens.configuration.ConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class ConfigurationFragment: BaseSettingsFragment(), BackAvailable, ProvidesTitle {

    companion object {
        private const val EXTRA_WIDGET_TYPE = "widget_type"
        private const val EXTRA_VARIANT = "variant"

        fun setup(intent: Intent, widgetType: WidgetType, variant: Variant) {
            intent.putExtra(EXTRA_WIDGET_TYPE, widgetType)
            intent.putExtra(EXTRA_VARIANT, variant)
        }

        fun getWidgetType(intent: Intent): WidgetType? {
            return intent.getSerializableExtraCompat(EXTRA_WIDGET_TYPE, WidgetType::class.java)
        }

        fun getVariant(intent: Intent): Variant? {
            return intent.getSerializableExtraCompat(EXTRA_VARIANT, Variant::class.java)
        }
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override val adapter by lazy {
        Adapter()
    }

    private val viewModel by viewModel<ConfigurationViewModel>()

    private val widgetType by lazy {
        getWidgetType(requireActivity().intent)
            ?: throw RuntimeException("Widget type not specified")
    }

    private val variant by lazy {
        getVariant(requireActivity().intent) ?: throw RuntimeException("Variant not specified")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        val id = requireActivity().intent.getStringExtra(SmartspacerConstants.EXTRA_SMARTSPACER_ID)
        viewModel.setupWithId(id ?: return)
    }

    override fun getTitle(): CharSequence {
        return getString(widgetType.configurationTitle)
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

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> = listOf(
        SwitchSetting(
            useStaticIcon,
            getString(R.string.configuration_use_static_title),
            getString(widgetType.configurationStaticIconContent),
            ContextCompat.getDrawable(requireContext(), widgetType.staticIcon)
        ) {
            viewModel.onUseStaticIconChanged(it, widgetType, variant)
        }
    )

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}