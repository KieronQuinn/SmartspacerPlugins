package com.kieronquinn.app.smartspacer.plugin.googlemaps.ui.screens.configuration

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.googlemaps.R
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository.TrafficLevel
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository.ZoomMode
import com.kieronquinn.app.smartspacer.plugin.googlemaps.targets.GoogleMapsTrafficTarget
import com.kieronquinn.app.smartspacer.plugin.googlemaps.ui.screens.configuration.GoogleMapsTrafficConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsAdapter
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.kieronquinn.app.shared.R as SharedR

class GoogleMapsTrafficConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    private val viewModel by viewModel<GoogleMapsTrafficConfigurationViewModel>()

    private val id by lazy {
        requireActivity().intent.getStringExtra(SmartspacerConstants.EXTRA_SMARTSPACER_ID)!!
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override val adapter by lazy {
        Adapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupState()
        viewModel.setupWithId(id)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
    }

    private fun setupState() {
        handleState(viewModel.state.value)
        whenResumed {
            viewModel.state.collect {
                handleState(it)
            }
        }
    }

    private fun handleState(state: State) {
        when(state) {
            is State.Loading -> {
                binding.settingsBaseLoading.isVisible = true
                binding.settingsBaseRecyclerView.isVisible = false
            }
            is State.Loaded -> {
                updateTarget()
                binding.settingsBaseLoading.isVisible = false
                binding.settingsBaseRecyclerView.isVisible = true
                adapter.update(state.loadItems(), binding.settingsBaseRecyclerView)
            }
        }
    }

    private fun State.Loaded.loadItems(): List<BaseSettingsItem> = listOfNotNull(
        GenericSettingsItem.Card(
            ContextCompat.getDrawable(requireContext(), SharedR.drawable.ic_info),
            getString(R.string.target_google_maps_traffic_setting_loading)
        ).takeIf { isLoading },
        GenericSettingsItem.Dropdown(
            getString(R.string.target_google_maps_traffic_setting_distance_title),
            getString(zoomMode.description),
            ContextCompat.getDrawable(
                requireContext(), R.drawable.ic_target_google_maps_traffic_configuration_zoom
            ),
            zoomMode,
            viewModel::onZoomModeChanged,
            ZoomMode.values().toList()
        ) {
            it.description
        },
        GenericSettingsItem.Dropdown(
            getString(R.string.target_google_maps_traffic_setting_min_traffic_level),
            getString(
                R.string.target_google_maps_traffic_setting_min_traffic_level_content,
                getString(minTrafficLevel.description)
            ),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_target_google_maps_traffic_configuration_level),
            minTrafficLevel,
            viewModel::onTrafficLevelChanged,
            TrafficLevel.values().toList()
        ) {
            it.description
        }
    )

    private fun updateTarget() {
        SmartspacerTargetProvider.notifyChange(
            requireContext(), GoogleMapsTrafficTarget::class.java, id
        )
    }

    inner class Adapter: BaseSettingsAdapter(binding.settingsBaseRecyclerView, emptyList())

}