package com.kieronquinn.app.smartspacer.plugin.youtube.complications

import android.content.Context
import android.content.Intent
import android.icu.text.CompactDecimalFormat
import android.net.Uri
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugin.youtube.R
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeRepository.SubscriberCount
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.activities.ConfigurationActivity.NavGraphMapping.COMPLICATION_SUBSCRIPTIONS
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import java.util.Locale
import android.graphics.drawable.Icon as AndroidIcon


class SubscriberComplication: SmartspacerComplicationProvider() {

    private val dataRepository by inject<DataRepository>()
    private val gson by inject<Gson>()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val complicationData = getComplicationData(smartspacerId)
        return listOfNotNull(
            complicationData.toSmartspaceAction(smartspacerId, !complicationData.showFullFormat)
        )
    }

    private fun ComplicationData.toSmartspaceAction(
        smartspacerId: String,
        formatNumber: Boolean
    ): SmartspaceAction? {
        val subscriberCount = (subscriberCount as? SubscriberCount.Count)
            ?: lastSuccessfulSubscriberCount
            ?: return null
        val text = subscriberCount.count.let {
            if(formatNumber) it.formatNumber() else it
        }
        return ComplicationTemplate.Basic(
            "youtube_subscriptions_$smartspacerId",
            Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_youtube)),
            Text(text),
            getTapAction()
        ).create()
    }

    private fun ComplicationData.getTapAction(): TapAction {
        val intent = channelId?.let {
            Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://youtube.com/channel/$channelId")
            }
        }
        return TapAction(intent = intent)
    }

    private fun ComplicationData.getDescription(): String? {
        val channelName = subscriberCount?.getChannelNameOrNull() ?: return null
        return resources.getString(R.string.complication_subscriber_count_content_set, channelName)
    }

    private fun getComplicationData(smartspacerId: String): ComplicationData {
        return dataRepository.getComplicationData(smartspacerId, ComplicationData::class.java)
            ?: ComplicationData()
    }

    override fun getConfig(smartspacerId: String?): Config {
        val complicationData = smartspacerId?.let { getComplicationData(it) }
        val description = complicationData?.getDescription()
            ?: resources.getString(R.string.complication_subscriber_count_content)
        return Config(
            resources.getString(R.string.complication_subscriber_count_label),
            description,
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_youtube),
            refreshIfNotVisible = true,
            refreshPeriodMinutes = complicationData?.refreshRate?.minutes ?: 0,
            configActivity = createIntent(provideContext(), COMPLICATION_SUBSCRIPTIONS)
        )
    }

    override fun createBackup(smartspacerId: String): Backup {
        val data = getComplicationData(smartspacerId)
        return Backup(gson.toJson(data), data.getDescription())
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val data = try {
            gson.fromJson(backup.data, ComplicationData::class.java)
        }catch (e: Exception){
            null
        } ?: return false
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onRestored
        ) {
            data
        }
        return true
    }

    private fun onRestored(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    private fun String.formatNumber(): String {
        val number = toLongOrNull() ?: return this
        val compactDecimalFormat = CompactDecimalFormat.getInstance(
            Locale.getDefault(), CompactDecimalFormat.CompactStyle.SHORT
        )
        return compactDecimalFormat.format(number)
    }

    data class ComplicationData(
        @SerializedName("channel_id")
        val channelId: String? = null,
        @SerializedName("subscriber_count")
        val subscriberCount: SubscriberCount? = null,
        @SerializedName("last_successful_subscriber_count")
        val lastSuccessfulSubscriberCount: SubscriberCount.Count? = null,
        @SerializedName("refresh_rate")
        val refreshRate: RefreshRate = RefreshRate.TWELVE_HOURS,
        @SerializedName("show_full_format")
        val showFullFormat: Boolean = false
    ) {

        companion object {
            const val TYPE = "subscriber_count"
        }

        enum class RefreshRate(val minutes: Int, @StringRes val label: Int) {
            THIRTY_MINUTES(30, R.string.refresh_rate_30_minutes),
            ONE_HOUR(60, R.string.refresh_rate_60_minutes),
            TWO_HOURS(120, R.string.refresh_rate_120_minutes),
            THREE_HOURS(180, R.string.refresh_rate_180_minutes),
            SIX_HOURS(360, R.string.refresh_rate_360_minutes),
            TWELVE_HOURS(720, R.string.refresh_rate_720_minutes),
            ONE_DAY(1440, R.string.refresh_rate_1440_minutes),
        }

    }

}