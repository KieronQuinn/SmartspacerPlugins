package com.kieronquinn.app.smartspacer.plugin.countdown.complications

import android.content.Context
import android.net.Uri
import android.text.format.DateFormat
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.countdown.R
import com.kieronquinn.app.smartspacer.plugin.countdown.providers.FontIconProvider
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
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
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import kotlin.math.abs
import kotlin.math.absoluteValue
import android.graphics.drawable.Icon as AndroidIcon
import com.kieronquinn.app.smartspacer.plugin.countdown.model.Icon as CountdownIcon

class CountdownComplication: SmartspacerComplicationProvider() {

    private val dateFormat by lazy {
        DateFormat.getDateFormat(provideContext())
    }

    private val dataRepository by inject<DataRepository>()
    private val gson by inject<Gson>()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val data = getComplicationData(smartspacerId)
        return data?.endLocalDate?.let {
            getComplication(smartspacerId, it, data.icon, data.countUp)
        } ?: emptyList()
    }

    private fun getComplication(
        smartspacerId: String,
        endTime: LocalDate,
        icon: CountdownIcon,
        allowCountUp: Boolean
    ): List<SmartspaceAction> {
        val daysUtil = endTime.getDaysLeft(allowCountUp).toInt()
        val content = resources.getQuantityString(
            R.plurals.complication_countdown, daysUtil, daysUtil
        )
        return listOf(
            ComplicationTemplate.Basic(
                "countdown_${smartspacerId}_${abs(icon.hashCode())}",
                icon.toIcon(),
                Text(content),
                TapAction()
            ).create()
        )
    }

    private fun CountdownIcon.toIcon(): Icon {
        val icon = when(this){
            is CountdownIcon.File -> {
                AndroidIcon.createWithContentUri(Uri.parse(uri))
            }
            is CountdownIcon.Font -> {
                AndroidIcon.createWithContentUri(FontIconProvider.createUri(this))
            }
        }
        val shouldTint = when(this) {
            is CountdownIcon.Font -> true
            is CountdownIcon.File -> tint
        }
        return Icon(icon, shouldTint = shouldTint)
    }

    override fun getConfig(smartspacerId: String?): Config {
        val data = smartspacerId?.let { getComplicationData(it) }
        val description = if(data?.endDate != null){
            resources.getString(
                R.string.complication_countdown_description_set,
                data.endLocalDate?.formatTime()
            )
        }else{
            resources.getString(R.string.complication_countdown_description)
        }
        return Config(
            resources.getString(R.string.complication_countdown_label),
            description,
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_countdown),
            allowAddingMoreThanOnce = true,
            configActivity = createIntent(provideContext(), NavGraphMapping.COMPLICATION_COUNTDOWN),
            setupActivity = createIntent(provideContext(), NavGraphMapping.COMPLICATION_COUNTDOWN)
        )
    }

    override fun createBackup(smartspacerId: String): Backup {
        val data = getComplicationData(smartspacerId)
        val description = if(data?.endDate != null){
            resources.getString(
                R.string.complication_countdown_description_set,
                data.endLocalDate?.formatTime()
            )
        }else{
            resources.getString(R.string.complication_countdown_description)
        }
        //Don't backup file-based icons as they cannot be restored
        val backupData = data?.copy(
            icon = data.icon as? CountdownIcon.Font ?: CountdownIcon.default()
        )
        return Backup(gson.toJson(backupData), description)
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
        return true
    }

    private fun onRestored(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    override fun onProviderRemoved(smartspacerId: String) {
        super.onProviderRemoved(smartspacerId)
        dataRepository.deleteComplicationData(smartspacerId)
    }

    private fun getComplicationData(smartspacerId: String): ComplicationData? {
        return dataRepository.getComplicationData(smartspacerId, ComplicationData::class.java)
    }

    private fun LocalDate.formatTime(): String {
        val instant = atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()
        val date = Date.from(instant)
        return dateFormat.format(date)
    }

    private fun LocalDate.getDaysLeft(allowNegative: Boolean): Long {
        val now = LocalDate.now()
        return ChronoUnit.DAYS.between(now, this).let {
            if(allowNegative) it.absoluteValue else it.coerceAtLeast(0L)
        }
    }

    data class ComplicationData(
        @SerializedName("end_time")
        val endDate: String? = null,
        @SerializedName("icon")
        val icon: CountdownIcon = CountdownIcon.default(),
        @SerializedName("allow_count_up")
        val allowCountUp: Boolean? = null
    ) {

        val endLocalDate: LocalDate?
            get() {
                return endDate?.let {
                    LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE)
                }
            }

        val endTimestamp
            get() = endLocalDate?.atStartOfDay()
                ?.atZone(ZoneId.systemDefault())
                ?.toInstant()
                ?.toEpochMilli()

        val countUp: Boolean
            get() = allowCountUp ?: false

        companion object {
            const val TYPE = "countdown"
        }

    }

}