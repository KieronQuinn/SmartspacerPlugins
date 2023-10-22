package com.kieronquinn.app.smartspacer.plugins.datausage.complications

import android.app.usage.NetworkStats
import android.app.usage.NetworkStats.Bucket
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.provider.Settings
import android.text.format.Formatter
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugins.datausage.R
import com.kieronquinn.app.smartspacer.plugins.datausage.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugins.datausage.utils.extensions.getDayOrMax
import com.kieronquinn.app.smartspacer.plugins.datausage.utils.extensions.hasDataUsagePermission
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import android.graphics.drawable.Icon as AndroidIcon

class DataUsageComplication: SmartspacerComplicationProvider() {

    private val dataRepository by inject<DataRepository>()
    private val gson by inject<Gson>()

    private val networkStatsManager by lazy {
        provideContext().getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    }

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val data = getComplicationData(smartspacerId)
        if(!provideContext().hasDataUsagePermission()) return emptyList()
        return listOfNotNull(data.toComplication(smartspacerId))
    }

    override fun getConfig(smartspacerId: String?): Config {
        val data = smartspacerId?.let { getComplicationData(it) }
        val description = if(data != null) {
            resources.getString(
                R.string.complication_description_set,
                resources.getString(data.network.label)
            )
        }else resources.getString(R.string.complication_description)
        return Config(
            resources.getString(R.string.complication_label),
            description,
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_data_usage),
            refreshPeriodMinutes = data?.refreshRate?.minutes ?: 0,
            allowAddingMoreThanOnce = true,
            setupActivity = createIntent(provideContext(), NavGraphMapping.COMPLICATION_DATA_USAGE),
            configActivity = createIntent(provideContext(), NavGraphMapping.COMPLICATION_DATA_USAGE)
        )
    }

    override fun onProviderRemoved(smartspacerId: String) {
        dataRepository.deleteComplicationData(smartspacerId)
    }

    override fun createBackup(smartspacerId: String): Backup {
        val data = getComplicationData(smartspacerId)
        val description = resources.getString(
            R.string.complication_description_set,
            resources.getString(data.network.label)
        )
        return Backup(gson.toJson(data), description)
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val data = try {
            gson.fromJson(backup.data, ComplicationData::class.java)
        }catch (e: Exception) {
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
        return provideContext().hasDataUsagePermission() //Request permission if needed
    }

    private fun onRestored(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    private fun getComplicationData(smartspacerId: String): ComplicationData {
        return dataRepository.getComplicationData(smartspacerId, ComplicationData::class.java)
            ?: ComplicationData()
    }

    private fun ComplicationData.toComplication(smartspacerId: String): SmartspaceAction? {
        val startTime = getStartTime().toInstant().toEpochMilli()
        val details = try {
            networkStatsManager.querySummary(
                network.networkType,
                null,
                startTime,
                System.currentTimeMillis()
            )
        }catch (e: Exception) {
            return null
        } ?: return null
        return details.toComplication(smartspacerId)
    }

    private fun NetworkStats.toComplication(smartspacerId: String): SmartspaceAction {
        return ComplicationTemplate.Basic(
            "${smartspacerId}_data_usage_at_${System.currentTimeMillis()}",
            Icon(AndroidIcon.createWithResource(provideContext(), R.drawable.ic_data_usage)),
            Text(getTotalBytes()),
            TapAction(intent = Intent(Settings.ACTION_DATA_USAGE_SETTINGS))
        ).create()
    }

    private fun NetworkStats.getTotalBytes(): String {
        val buckets = ArrayList<Bucket>()
        while(hasNextBucket()){
            val bucket = Bucket()
            getNextBucket(bucket)
            buckets.add(bucket)
        }
        return buckets.sumOf { it.rxBytes + it.txBytes }.let {
            Formatter.formatFileSize(provideContext(), it)
        }
    }

    private fun ComplicationData.getStartTime(): ZonedDateTime {
        val now = LocalDate.now()
        return when {
            cycleDay <= now.dayOfMonth -> {
                now.withDayOfMonth(cycleDay).atStartOfDay(ZoneId.systemDefault())
            }
            else -> {
                val lastMonth = now.minusMonths(1).let {
                    it.withDayOfMonth(it.month.getDayOrMax(cycleDay))
                }
                lastMonth.atStartOfDay(ZoneId.systemDefault())
            }
        }
    }

    data class ComplicationData(
        val cycleDay: Int = 1,
        val network: Network = Network.MOBILE_DATA,
        val refreshRate: RefreshRate = RefreshRate.THIRTY_MINUTES
    ) {
        companion object {
            const val TYPE = "data_usage"
        }

        @Suppress("DEPRECATION")
        enum class Network(val networkType: Int, @StringRes val label: Int) {
            MOBILE_DATA(ConnectivityManager.TYPE_MOBILE, R.string.network_type_mobile_data),
            WIFI(ConnectivityManager.TYPE_WIFI, R.string.network_type_wifi),
            ETHERNET(ConnectivityManager.TYPE_ETHERNET, R.string.network_type_ethernet)
        }

        enum class RefreshRate(val minutes: Int, @StringRes val label: Int) {
            FIFTEEN_MINUTES(15, R.string.refresh_rate_15_mins),
            THIRTY_MINUTES(30, R.string.refresh_rate_30_mins),
            ONE_HOUR(60, R.string.refresh_rate_60_mins),
            TWO_HOURS(120, R.string.refresh_rate_120_mins),
        }
    }

}