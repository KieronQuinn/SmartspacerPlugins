package com.kieronquinn.app.smartspacer.plugin.googlehealth.complications

import android.content.Context
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.googlehealth.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.googlehealth.GoogleHealthPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.googlehealth.R
import com.kieronquinn.app.smartspacer.plugin.googlehealth.complications.GoogleHealthComplication.ComplicationData.TapAction.OPEN_METRIC
import com.kieronquinn.app.smartspacer.plugin.googlehealth.complications.GoogleHealthComplication.ComplicationData.TapAction.REFRESH
import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.HealthMetric
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.GoogleHealthRepository
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.GoogleHealthRepository.HealthItem
import com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.googlehealth.widgets.GoogleHealthWidget
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.sdk.SmartspacerConstants
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

class GoogleHealthComplication: SmartspacerComplicationProvider() {

    private val dataRepository by inject<DataRepository>()
    private val googleHealthRepository by inject<GoogleHealthRepository>()
    private val gson by inject<Gson>()

    private val packageManager by lazy {
        provideContext().packageManager
    }

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val config = getConfiguration(smartspacerId)
        val metric = config.metric
        val healthItem = metric?.let {
            googleHealthRepository.getHealthItem(it)
        }
        val value = when {
            metric == null -> resources.getString(R.string.metric_setup)
            healthItem != null -> if (healthItem.isRefreshing) {
                resources.getString(R.string.metric_refreshing)
            } else {
                healthItem.value
            }
            else -> resources.getString(R.string.metric_not_available)
        }
        val clickAction = getTapAction(smartspacerId, healthItem, config.action)
        val icon = metric?.iconOutline ?: R.drawable.ic_google_health
        if (healthItem?.isStale(config) == true) return emptyList()
        return ComplicationTemplate.Basic(
            "${smartspacerId}_at_${System.currentTimeMillis()}",
            Icon(AndroidIcon.createWithResource(provideContext(), icon)),
            Text(value),
            clickAction
        ).let { listOf(it.create()) }
    }

    private fun getTapAction(
        smartspacerId: String,
        healthItem: HealthItem?,
        requested: ComplicationData.TapAction
    ): TapAction {
        return when {
            healthItem == null -> {
                TapAction(intent = getConfigIntent().apply {
                    putExtra(SmartspacerConstants.EXTRA_SMARTSPACER_ID, smartspacerId)
                })
            }
            requested == OPEN_METRIC && healthItem.clickIntent != null -> {
                TapAction(pendingIntent = healthItem.clickIntent)
            }
            requested == REFRESH && healthItem.refreshIntent != null -> {
                TapAction(pendingIntent = healthItem.refreshIntent)
            }
            else -> {
                TapAction(intent = packageManager.getLaunchIntentForPackage(PACKAGE_NAME))
            }
        }
    }

    override fun getConfig(smartspacerId: String?): Config {
        val config = smartspacerId?.let { getConfiguration(smartspacerId) }
        val icon = config?.metric?.iconOutline ?: R.drawable.ic_google_health
        return Config(
            label = resources.getString(R.string.complication_google_health_title),
            description = getDescription(config),
            icon = AndroidIcon.createWithResource(provideContext(), icon),
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widgets.health",
            compatibilityState = getCompatibilityState(),
            configActivity = getConfigIntent(),
            //Refresh by a factor of the timeouts to hide after the time period
            refreshPeriodMinutes = 15,
            allowAddingMoreThanOnce = true
        )
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(GoogleHealthWidget.getProvider(provideContext()) == null) {
            CompatibilityState.Incompatible(resources.getString(R.string.complication_google_health_incompatible))
        }else CompatibilityState.Compatible
    }

    private fun getDescription(config: ComplicationData?): String {
        return if (config?.metric != null) {
            resources.getString(
                R.string.complication_google_health_description,
                resources.getString(config.metric.label)
            )
        } else {
            resources.getString(R.string.complication_google_health_description_unset)
        }
    }

    override fun createBackup(smartspacerId: String): Backup {
        val config = getConfiguration(smartspacerId)
        val description = getDescription(config)
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
            ComplicationData(config.metric, config.timeoutEnabled, config.timeout)
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

    private fun HealthItem.isStale(config: ComplicationData): Boolean {
        if (!config.timeoutEnabled) return false
        return Duration.between(instant, Instant.now()).abs() > config.timeout.duration
    }

    private fun getConfigIntent() = createIntent(provideContext(), NavGraphMapping.HEALTH)

    data class ComplicationData(
        @SerializedName("metric")
        val metric: HealthMetric? = null,
        @SerializedName("timeout_enabled")
        val timeoutEnabled: Boolean = false,
        @SerializedName("timeout")
        val timeout: Timeout = Timeout.SIXTY_MINUTES,
        @SerializedName("tap_action")
        val action: TapAction = OPEN_METRIC
    ) {

        companion object {
            const val TYPE = "sleep"
        }

        enum class Timeout(minutes: Int, @StringRes val label: Int) {
            FIFTEEN_MINUTES(15, R.string.timeout_15_minutes),
            THIRTY_MINUTES(30, R.string.timeout_30_minutes),
            SIXTY_MINUTES(60, R.string.timeout_60_minutes),
            ONE_HUNDRED_TWENTY_MINUTES(120, R.string.timeout_120_minutes);

            val duration: Duration? = Duration.ofMinutes(minutes.toLong())
        }

        enum class TapAction(@StringRes val label: Int) {
            OPEN_METRIC(R.string.tap_action_open_metric),
            OPEN_APP(R.string.tap_action_open_app),
            REFRESH(R.string.tap_action_refresh)
        }

    }

}