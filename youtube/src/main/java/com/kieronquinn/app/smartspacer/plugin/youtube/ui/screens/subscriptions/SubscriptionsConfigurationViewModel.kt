package com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItem
import com.kieronquinn.app.smartspacer.plugin.shared.model.settings.BaseSettingsItemType
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.youtube.complications.SubscriberComplication
import com.kieronquinn.app.smartspacer.plugin.youtube.complications.SubscriberComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.youtube.complications.SubscriberComplication.ComplicationData.RefreshRate
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeRepository
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeSettingsRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

abstract class SubscriptionsConfigurationViewModel: ViewModel() {

    abstract val state: StateFlow<State>

    abstract fun setup(smartspacerId: String)
    abstract fun onApiKeyClicked()
    abstract fun onApiKeyChanged(apiKey: String)
    abstract fun onChannelIdClicked()
    abstract fun onChannelIdChanged(channelId: String)
    abstract fun onShowFullNumberChanged(enabled: Boolean)
    abstract fun onRefreshRateChanged(refreshRate: RefreshRate)
    abstract fun onRefreshClicked()
    abstract fun onWikiPageClicked()

    sealed class State {
        data object Loading: State()
        data class Loaded(val apiKey: String, val complicationData: ComplicationData): State() {
            override fun equals(other: Any?): Boolean {
                return false
            }

            override fun hashCode(): Int {
                var result = apiKey.hashCode()
                result = 31 * result + complicationData.hashCode()
                return result
            }
        }
    }

    sealed class SubscriptionsConfigurationSettingsItem(val type: ItemType): BaseSettingsItem(type) {

        data class Footer(val onLinkClicked: () -> Unit):
            SubscriptionsConfigurationSettingsItem(ItemType.FOOTER)

        enum class ItemType: BaseSettingsItemType {
            FOOTER
        }
    }

}

class SubscriptionsConfigurationViewModelImpl(
    private val dataRepository: DataRepository,
    private val navigation: ContainerNavigation,
    private val youTubeRepository: YouTubeRepository,
    settings: YouTubeSettingsRepository
): SubscriptionsConfigurationViewModel() {

    companion object {
        private const val URL_WIKI = "https://kieronquinn.co.uk/redirect/smartspacer/plugins/youtube"
    }

    private val apiKey = settings.apiKey
    private val smartspacerId = MutableStateFlow<String?>(null)

    private val complicationData = smartspacerId.filterNotNull().flatMapLatest {
        dataRepository.getComplicationDataFlow(it, ComplicationData::class.java)
    }

    override val state = combine(
        complicationData,
        apiKey.asFlow()
    ) { data, key ->
        State.Loaded(key, data ?: ComplicationData())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    override fun setup(smartspacerId: String) {
        viewModelScope.launch {
            this@SubscriptionsConfigurationViewModelImpl.smartspacerId.emit(smartspacerId)
        }
    }

    override fun onApiKeyClicked() {
        val current = (state.value as? State.Loaded)?.apiKey ?: ""
        viewModelScope.launch {
            navigation.navigate(SubscriptionsConfigurationFragmentDirections
                .actionSubscriptionsConfigurationFragmentToApiKeyFragment(current))
        }
    }

    override fun onApiKeyChanged(apiKey: String) {
        viewModelScope.launch {
            this@SubscriptionsConfigurationViewModelImpl.apiKey.set(apiKey)
            onRefreshClicked()
        }
    }

    override fun onChannelIdClicked() {
        val current = (state.value as? State.Loaded)?.complicationData?.channelId ?: ""
        viewModelScope.launch {
            navigation.navigate(SubscriptionsConfigurationFragmentDirections
                .actionSubscriptionsConfigurationFragmentToChannelIdFragment(current))
        }
    }

    override fun onChannelIdChanged(channelId: String) {
        updateData {
            copy(channelId = channelId)
        }
    }

    override fun onShowFullNumberChanged(enabled: Boolean) {
        updateData(false) {
            copy(showFullFormat = enabled)
        }
    }

    override fun onRefreshRateChanged(refreshRate: RefreshRate) {
        updateData(false) {
            copy(refreshRate = refreshRate)
        }
    }

    private fun updateData(refresh: Boolean = true, block: ComplicationData.() -> ComplicationData) {
        val smartspacerId = smartspacerId.value ?: return
        val onUpdate: (Context, String) -> Unit = { context, _ ->
            if(refresh){
                onRefreshClicked()
            }else{
                SmartspacerComplicationProvider.notifyChange(
                    context, SubscriberComplication::class.java, smartspacerId
                )
            }
        }
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            onUpdate
        ) {
            val data = it ?: ComplicationData()
            block(data)
        }
    }

    override fun onRefreshClicked() {
        val smartspacerId = smartspacerId.value ?: return
        val state = (state.value as? State.Loaded) ?: return
        if(state.apiKey.isBlank()) return
        if(state.complicationData.channelId.isNullOrBlank()) return
        viewModelScope.launch {
            youTubeRepository.updateSubscriberCountNow(smartspacerId)
        }
    }

    override fun onWikiPageClicked() {
        viewModelScope.launch {
            navigation.navigate(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(URL_WIKI)
            })
        }
    }

}