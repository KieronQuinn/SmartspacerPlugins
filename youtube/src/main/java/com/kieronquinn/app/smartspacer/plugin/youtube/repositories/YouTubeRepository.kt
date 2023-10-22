package com.kieronquinn.app.smartspacer.plugin.youtube.repositories

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.youtube.complications.SubscriberComplication
import com.kieronquinn.app.smartspacer.plugin.youtube.complications.SubscriberComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.youtube.model.YouTubeStatisticsResponse
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeRepository.SubscriberCount
import com.kieronquinn.app.smartspacer.plugin.youtube.service.YouTubeService
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

interface YouTubeRepository {

    fun updateSubscriberCount(smartspacerId: String)
    suspend fun updateSubscriberCountNow(smartspacerId: String): SubscriberCount

    sealed class SubscriberCount(
        @SerializedName(NAME_TYPE)
        val type: Type,
        @SerializedName("refreshed_at")
        val refreshedAt: Long = System.currentTimeMillis()
    ) {
        companion object {
            const val NAME_TYPE = "type"
        }

        data class Count(
            @SerializedName("count")
            val count: String,
            @SerializedName("channel_name")
            val channelName: String
        ): SubscriberCount(Type.COUNT) {
            override fun getChannelNameOrNull() = channelName
        }

        data class Hidden(
            @SerializedName("channel_name")
            val channelName: String
        ): SubscriberCount(Type.HIDDEN) {
            override fun getChannelNameOrNull() = channelName
        }

        data object InvalidApiKey: SubscriberCount(Type.INVALID_API_KEY)
        data object InvalidId: SubscriberCount(Type.INVALID_ID)
        data object Error: SubscriberCount(Type.ERROR)

        open fun getChannelNameOrNull(): String? = null

        enum class Type {
            COUNT, HIDDEN, INVALID_API_KEY, INVALID_ID, ERROR
        }
    }

}

class YouTubeRepositoryImpl(
    private val settings: YouTubeSettingsRepository,
    private val dataRepository: DataRepository,
    private val service: YouTubeService,
    private val context: Context
): YouTubeRepository {

    private val scope = MainScope()

    override fun updateSubscriberCount(smartspacerId: String) {
        scope.launch {
            updateSubscriberCountNow(smartspacerId)
        }
    }

    override suspend fun updateSubscriberCountNow(smartspacerId: String): SubscriberCount {
        val subscriberCount = dataRepository.getComplicationData(
            smartspacerId, ComplicationData::class.java
        )?.channelId?.let {
            getSubscriberCount(it)
        } ?: SubscriberCount.InvalidId
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onComplicationUpdated
        ) {
            val data = it ?: ComplicationData()
            val lastSuccessful = subscriberCount.takeIf { count ->
                count is SubscriberCount.Count
            } ?: data.lastSuccessfulSubscriberCount
            data.copy(
                subscriberCount = subscriberCount,
                lastSuccessfulSubscriberCount = lastSuccessful as? SubscriberCount.Count
            )
        }
        return subscriberCount
    }

    private fun onComplicationUpdated(context: Context, smartspacerId: String) {
        SmartspacerComplicationProvider.notifyChange(
            context,
            SubscriberComplication::class.java,
            smartspacerId
        )
    }

    private suspend fun getSubscriberCount(channelId: String): SubscriberCount {
        return withContext(Dispatchers.IO) {
            val apiKey = settings.apiKey.get().takeIf { it.isNotBlank() }
                ?: run {
                    return@withContext SubscriberCount.InvalidApiKey
                }
            try {
                val response = service.getStatistics(apiKey, channelId).execute()
                when {
                    response.code() == 400 -> SubscriberCount.InvalidApiKey
                    else -> response.body()?.getSubscriberCount()
                }
            }catch (e: Exception) {
                null
            } ?: SubscriberCount.Error
        }
    }

    private fun YouTubeStatisticsResponse.getSubscriberCount(): SubscriberCount {
        if(items.isEmpty()) return SubscriberCount.InvalidId
        return items.first().let {
            val name = it.snippet.title
            val statistics = it.statistics
            if(statistics.hiddenSubscriberCount){
                SubscriberCount.Hidden(name)
            }else{
                SubscriberCount.Count(statistics.subscriberCount, name)
            }
        }
    }

}