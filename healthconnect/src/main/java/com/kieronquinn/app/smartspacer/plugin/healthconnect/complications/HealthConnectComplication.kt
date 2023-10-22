package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import android.content.Context
import android.content.Intent
import androidx.health.connect.client.records.Record
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.RefreshPeriod
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.TimeoutPeriod
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.UnitType
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.HealthConnectRepository
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.HealthConnectRepository.HealthMetric
import com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.configuration.ConfigurationFragment
import com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.setup.SetupFragment
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.graphics.drawable.Icon as AndroidIcon

abstract class HealthConnectComplication<T: Record>(
    private val dataType: DataType
): SmartspacerComplicationProvider() {

    abstract val authority: String

    private val dataRepository by inject<DataRepository>()
    private val healthConnectRepository by inject<HealthConnectRepository>()
    private val gson by inject<Gson>()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val config = getConfiguration(smartspacerId)
        val metric = healthConnectRepository.getHealthData(smartspacerId) ?: return emptyList()
        val id = "health_connect_${dataType.name.lowercase()}_$smartspacerId"
        return when(metric) {
            is HealthMetric.Metric -> {
                metric.toComplication(smartspacerId, config.openPackage)
            }
            is HealthMetric.NoPermission -> {
                getNoPermissionComplication(id)
            }
        }.let { listOfNotNull(it) }
    }

    private fun HealthMetric.Metric.toComplication(
        id: String,
        openPackage: String?
    ): SmartspaceAction {
        return ComplicationTemplate.Basic(
            id = id,
            content = Text(value ?: resources.getString(R.string.complication_content_blank)),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), dataType.icon)),
            onClick = TapAction(intent = getLaunchIntentForPackage(openPackage, fromPackage))
        ).create()
    }

    private fun getNoPermissionComplication(id: String): SmartspaceAction {
        return ComplicationTemplate.Basic(
            id = id,
            content = Text(resources.getString(R.string.complication_content_no_permission)),
            icon = Icon(AndroidIcon.createWithResource(provideContext(), dataType.icon)),
            onClick = TapAction(intent = createConfigIntent())
        ).create()
    }

    override fun getConfig(smartspacerId: String?): Config {
        val description = resources.getString(
            R.string.complication_description,
            resources.getString(dataType.description)
        )
        val config = getConfiguration(smartspacerId)
        return Config(
            allowAddingMoreThanOnce = true,
            label = resources.getString(dataType.label),
            description = description,
            compatibilityState = getCompatibilityState(),
            icon = AndroidIcon.createWithResource(provideContext(), dataType.icon),
            setupActivity = createSetupIntent(),
            configActivity = createConfigIntent(),
            refreshPeriodMinutes = config.refreshPeriod.duration.toMinutes().toInt(),
            refreshIfNotVisible = true
        )
    }

    override fun onProviderRemoved(smartspacerId: String) {
        healthConnectRepository.removeData(smartspacerId)
    }

    override fun createBackup(smartspacerId: String): Backup {
        val config = getConfig(smartspacerId)
        return Backup(gson.toJson(config))
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val config = try {
            gson.fromJson(backup.data, ComplicationData::class.java)
        }catch (e: Exception){
            return false
        } ?: return false
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onChanged
        ) {
            val data = it ?: ComplicationData(dataType)
            data.copy(
                openPackage = config.openPackage,
                timeout = config.timeout,
                _resetTime = config.resetTime?.format(DateTimeFormatter.ISO_LOCAL_TIME),
                refreshPeriod = config.refreshPeriod,
                unitType = config.unitType
            )
        }
        return false //Still require setup to ask for permissions
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        notifyChange(context, this::class.java, smartspacerId)
    }

    private fun createSetupIntent(): Intent {
        return BaseConfigurationActivity.createIntent(
            provideContext(), NavGraphMapping.SETUP
        ).apply {
            SetupFragment.applyConfig(this, dataType, authority)
        }
    }

    private fun createConfigIntent(): Intent {
        return BaseConfigurationActivity.createIntent(
            provideContext(), NavGraphMapping.CONFIGURATION
        ).apply {
            ConfigurationFragment.applyConfig(this, dataType)
        }
    }

    private fun getConfiguration(smartspacerId: String?): ComplicationData {
        return smartspacerId?.let {
            dataRepository.getComplicationData(smartspacerId, ComplicationData::class.java)
        } ?: ComplicationData(dataType)
    }

    private fun getCompatibilityState(): CompatibilityState {
        return if(!healthConnectRepository.isSdkAvailable()) {
            CompatibilityState.Incompatible(resources.getString(R.string.complication_incompatible))
        }else CompatibilityState.Compatible
    }

    private fun getLaunchIntentForPackage(packageName: String?, fallback: String?): Intent? {
        if(packageName == null && fallback == null) return null
        return packageName?.let { provideContext().packageManager.getLaunchIntentForPackage(it) }
            ?: fallback?.let { provideContext().packageManager.getLaunchIntentForPackage(it) }
    }

    data class ComplicationData(
        @SerializedName("data_type")
        val dataType: DataType,
        @SerializedName("reset_time")
        val _resetTime: String? = dataType.getDefaultResetTime(),
        @SerializedName("timeout")
        val timeout: TimeoutPeriod? = dataType.getDefaultTimeout(),
        @SerializedName("refresh_period")
        val refreshPeriod: RefreshPeriod = RefreshPeriod.FIFTEEN_MINUTES,
        @SerializedName("open_package")
        val openPackage: String? = null,
        @SerializedName("unit_type")
        val unitType: String? = null
    ) {

        companion object {
            const val TYPE = "health_connect"
        }

        val resetTime
            get() = _resetTime?.let { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }

        fun <U: UnitType> getUnitOrNull(): U? {
            val constants = dataType.unitType?.java?.enumConstants ?: return null
            if(constants.isEmpty()) return null
            val name = unitType ?: constants.first().name
            return constants.firstOrNull { it.name == name } as? U
        }

    }

}