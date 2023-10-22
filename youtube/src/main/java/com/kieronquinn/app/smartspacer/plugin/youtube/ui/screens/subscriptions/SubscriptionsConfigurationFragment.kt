package com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions

import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Card
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Dropdown
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.Setting
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.GenericSettingsItem.SwitchSetting
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.BackAvailable
import com.kieronquinn.app.smartspacer.plugin.shared.ui.base.settings.BaseSettingsFragment
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenResumed
import com.kieronquinn.app.smartspacer.plugin.youtube.R
import com.kieronquinn.app.smartspacer.plugin.youtube.complications.SubscriberComplication.ComplicationData.RefreshRate
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeRepository.SubscriberCount
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.SubscriptionsConfigurationViewModel.State
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.SubscriptionsConfigurationViewModel.SubscriptionsConfigurationSettingsItem
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.apikey.ApiKeyFragment.Companion.setupApiKeyResultListener
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.channelid.ChannelIdFragment.Companion.setupChannelIdResultListener
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants.EXTRA_SMARTSPACER_ID
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.Instant
import java.util.Date
import com.kieronquinn.app.shared.R as SharedR

class SubscriptionsConfigurationFragment: BaseSettingsFragment(), BackAvailable {

    private val viewModel by viewModel<SubscriptionsConfigurationViewModel>()

    private val dateFormat by lazy {
        DateFormat.getDateFormat(requireContext())
    }

    private val timeFormat by lazy {
        DateFormat.getTimeFormat(requireContext())
    }

    override val adapter by lazy {
        SubscriptionsConfigurationAdapter(binding.settingsBaseRecyclerView, emptyList())
    }

    override val additionalPadding by lazy {
        resources.getDimension(SharedR.dimen.margin_8)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupState()
        viewModel.setup(requireActivity().intent.getStringExtra(EXTRA_SMARTSPACER_ID)!!)
    }

    private fun setupListeners() {
        setupApiKeyResultListener {
            viewModel.onApiKeyChanged(it)
        }
        setupChannelIdResultListener {
            viewModel.onChannelIdChanged(it)
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
        val errorContent = when(complicationData.subscriberCount) {
            is SubscriberCount.Error -> R.string.subscriber_configuration_error_general
            is SubscriberCount.Hidden -> R.string.subscriber_configuration_error_hidden
            is SubscriberCount.InvalidApiKey -> R.string.subscriber_configuration_error_api_key
            is SubscriberCount.InvalidId -> R.string.subscriber_configuration_error_channel_id
            else -> null
        }
        val errorHeader = errorContent?.let {
            Card(ContextCompat.getDrawable(requireContext(), R.drawable.ic_warning), getString(it))
        }
        val channelId = complicationData.channelId?.takeIf { it.isNotBlank() }
        val refreshRate = complicationData.refreshRate
        return listOfNotNull(
            errorHeader,
            Setting(
                getString(R.string.subscriber_configuration_api_key_title),
                if(apiKey.isBlank()){
                    getText(R.string.subscriber_configuration_api_key_content)
                }else{
                    getString(R.string.subscriber_configuration_api_key_content_set)
                },
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_api_key),
                onClick = viewModel::onApiKeyClicked
            ),
            Setting(
                getString(R.string.subscriber_configuration_channel_id_title),
                channelId ?: getText(R.string.subscriber_configuration_channel_id_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_youtube),
                onClick = viewModel::onChannelIdClicked
            ),
            SwitchSetting(
                complicationData.showFullFormat,
                getString(R.string.subscriber_configuration_show_full_number_title),
                getString(R.string.subscriber_configuration_show_full_number_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_show_full_number),
                onChanged = viewModel::onShowFullNumberChanged
            ),
            Dropdown(
                getString(R.string.subscriber_configuration_refresh_rate_title),
                getString(
                    R.string.subscriber_configuration_refresh_rate_content,
                    getString(refreshRate.label)
                ),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_refresh_rate),
                refreshRate,
                viewModel::onRefreshRateChanged,
                RefreshRate.values().toList()
            ) {
                it.label
            },
            Setting(
                getString(R.string.subscriber_configuration_refresh_title),
                getString(R.string.subscriber_configuration_refresh_content),
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_refresh),
                onClick = viewModel::onRefreshClicked
            ).takeIf { apiKey.isNotBlank() && channelId != null },
            complicationData.subscriberCount?.getChannelInformation(channelId),
            SubscriptionsConfigurationSettingsItem.Footer(viewModel::onWikiPageClicked)
        )
    }

    private fun SubscriberCount.getChannelInformation(channelId: String?): Setting? {
        if(channelId == null) return null
        val channelName = getChannelNameOrNull()
            ?: getString(R.string.subscriber_configuration_info_content_unknown)
        val subscribers = when(this) {
            is SubscriberCount.Count -> count
            is SubscriberCount.Hidden -> {
                getString(R.string.subscriber_configuration_info_content_hidden)
            }
            else -> getString(R.string.subscriber_configuration_info_content_unknown)
        }
        val refreshed = Date.from(Instant.ofEpochMilli(refreshedAt))
        val refreshedAt = "${timeFormat.format(refreshed)}, ${dateFormat.format(refreshed)}"
        return Setting(
            getString(R.string.subscriber_configuration_info_title),
            getString(
                R.string.subscriber_configuration_info_content,
                channelName,
                channelId,
                subscribers,
                refreshedAt
            ),
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_info),
            isEnabled = false
        ){}
    }

}