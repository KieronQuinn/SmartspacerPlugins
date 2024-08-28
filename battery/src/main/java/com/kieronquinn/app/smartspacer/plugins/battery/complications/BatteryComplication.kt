package com.kieronquinn.app.smartspacer.plugins.battery.complications

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity.Companion.createIntent
import com.kieronquinn.app.smartspacer.plugins.battery.R
import com.kieronquinn.app.smartspacer.plugins.battery.model.BatteryLevels.BatteryLevel
import com.kieronquinn.app.smartspacer.plugins.battery.repositories.BatteryRepository
import com.kieronquinn.app.smartspacer.plugins.battery.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugins.battery.utils.extensions.makeSquare
import com.kieronquinn.app.smartspacer.plugins.battery.widgets.BatteryWidget
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.utils.ComplicationTemplate
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class BatteryComplication: SmartspacerComplicationProvider() {

    private val dataRepository by inject<DataRepository>()
    private val batteryRepository by inject<BatteryRepository>()
    private val gson by inject<Gson>()

    override fun getSmartspaceActions(smartspacerId: String): List<SmartspaceAction> {
        val complicationData = getComplicationData(smartspacerId)
        val batteryLevel = batteryRepository.getBatteryLevel(
            complicationData.name ?: return emptyList()
        ) ?: return emptyList()
        if(!batteryLevel.isConnected && !complicationData.showWhenDisconnected) {
            return emptyList()
        }
        return listOf(batteryLevel.toComplication(smartspacerId))
    }

    private fun BatteryLevel.toComplication(smartspacerId: String): SmartspaceAction {
        return ComplicationTemplate.Basic(
            "battery_${smartspacerId}_at_${System.currentTimeMillis()}",
            Icon(AndroidIcon.createWithBitmap(icon.makeSquare())),
            Text(getLabel(provideContext())),
            TapAction(intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        ).create()
    }

    override fun getConfig(smartspacerId: String?): Config {
        val complicationData = smartspacerId?.let { getComplicationData(it) }
        val description = if(complicationData?.name != null){
            resources.getString(R.string.complication_description_set, complicationData.name)
        }else{
            resources.getString(R.string.complication_description)
        }
        return Config(
            resources.getString(R.string.complication_label),
            description,
            AndroidIcon.createWithResource(provideContext(), R.drawable.ic_battery),
            allowAddingMoreThanOnce = true,
            widgetProvider = BatteryWidget.AUTHORITY,
            configActivity = createIntent(provideContext(), NavGraphMapping.COMPLICATION_BATTERY)
        )
    }

    override fun createBackup(smartspacerId: String): Backup {
        val complicationData = getComplicationData(smartspacerId)
        val description = if(complicationData.name != null){
            resources.getString(R.string.complication_description_set, complicationData.name)
        }else{
            resources.getString(R.string.complication_description)
        }
        return Backup(gson.toJson(complicationData), description)
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val complicationData = try {
            gson.fromJson(backup.data, ComplicationData::class.java)
        }catch (e: Exception) {
            null
        } ?: return false
        dataRepository.updateComplicationData(
            smartspacerId,
            ComplicationData::class.java,
            ComplicationData.TYPE,
            ::onChanged
        ) {
            complicationData
        }
        return true
    }

    private fun onChanged(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    override fun onProviderRemoved(smartspacerId: String) {
        super.onProviderRemoved(smartspacerId)
        dataRepository.deleteComplicationData(smartspacerId)
    }

    private fun getComplicationData(smartspacerId: String): ComplicationData {
        return dataRepository.getComplicationData(smartspacerId, ComplicationData::class.java)
            ?: ComplicationData()
    }

    data class ComplicationData(
        @SerializedName("name")
        val name: String? = null,
        @SerializedName("show_when_disconnected")
        val showWhenDisconnected: Boolean = false
    ) {

        companion object {
            const val TYPE = "battery"
        }

    }

}