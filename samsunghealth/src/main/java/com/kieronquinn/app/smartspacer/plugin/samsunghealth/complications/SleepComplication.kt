package com.kieronquinn.app.smartspacer.plugin.samsunghealth.complications

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.R
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.SamsungHealthPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories.SamsungHealthSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.widgets.SleepWidgetProvider
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import java.time.Duration
import java.time.Instant
import android.graphics.drawable.Icon as AndroidIcon

class SleepComplication: SmartspacerComplicationProvider() {

    companion object {
        private const val DEEP_LINK_URL =
            "https://shealth.samsung.com/deepLink?sc_id=tracker.sleep&action=view&destination=track&launch_dashboard=true"
    }

    private val dataRepository by inject<DataRepository>()
    private val settings by inject<SamsungHealthSettingsRepository>()
    private val gson by inject<Gson>()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val config = getConfiguration(smartspacerId)
        val now = Instant.now()
        if(config.timeoutEnabled){
            val timePosted = Instant.ofEpochMilli(settings.sleepTimestamp.getSync())
            val timeBetween = Duration.between(now, timePosted).abs()
            if(timeBetween.toMinutes() >= config.timeout.minutes) return emptyList()
        }
        return listOfNotNull(getSmartspaceActionForSleep(smartspacerId))
    }

    override fun onProviderRemoved(smartspacerId: String) {
        dataRepository.deleteComplicationData(smartspacerId)
    }

    private fun getSmartspaceActionForSleep(smartspacerId: String): SmartspaceAction? {
        val sleep = settings.sleepTime.getSync()
        if(sleep.isBlank()) return null
        val onClick = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(DEEP_LINK_URL)
            `package` = PACKAGE_NAME
        }
        return ComplicationTemplate.Basic(
            "samsung_health_sleep_$smartspacerId",
            Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_complication_sleep)),
            Text(sleep),
            onClick = TapAction(intent = onClick)
        ).create()
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = resources.getString(R.string.complication_sleep_title),
            description = resources.getString(R.string.complication_sleep_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_complication_sleep),
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.sleep",
            compatibilityState = getCompatibilityState(),
            configActivity = createIntent(provideContext(), NavGraphMapping.SLEEP),
            //Refresh by a factor of the timeouts to hide after the time period
            refreshPeriodMinutes = 15
        )
    }

    override fun createBackup(smartspacerId: String): Backup {
        val description = resources.getString(R.string.complication_sleep_description)
        val config = getConfiguration(smartspacerId)
        return Backup(gson.toJson(config), description)
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val config = try {
            gson.fromJson(backup.data, ComplicationData::class.java) ?: return false
        }catch (e: Exception) {
            return false
        }
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onChanged
        ) {
            ComplicationData(config.timeoutEnabled, config.timeout)
        }
        return true
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        notifyChange(context, this::class.java, smartspacerId)
    }

    private fun getConfiguration(smartspacerId: String): ComplicationData {
        return dataRepository.getComplicationData(smartspacerId, ComplicationData::class.java)
            ?: ComplicationData()
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(SleepWidgetProvider.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(resources.getString(R.string.complication_incompatible))
        }else CompatibilityState.Compatible
    }

    data class ComplicationData(
        @SerializedName("timeout_enabled")
        val timeoutEnabled: Boolean = true,
        @SerializedName("timeout")
        val timeout: Timeout = Timeout.SIXTY_MINUTES
    ) {

        companion object {
            const val TYPE = "sleep"
        }

        enum class Timeout(val minutes: Int, @StringRes val label: Int) {
            FIFTEEN_MINUTES(15, R.string.timeout_15_minutes),
            THIRTY_MINUTES(30, R.string.timeout_30_minutes),
            SIXTY_MINUTES(60, R.string.timeout_60_minutes),
            ONE_HUNDRED_TWENTY_MINUTES(120, R.string.timeout_120_minutes)
        }

    }

}