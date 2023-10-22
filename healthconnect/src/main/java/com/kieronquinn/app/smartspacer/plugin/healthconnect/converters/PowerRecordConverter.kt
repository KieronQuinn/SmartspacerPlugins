package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.PowerRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.Power
import androidx.health.connect.client.units.Power as HCPower

object PowerRecordConverter: DataTypeConverter<PowerRecord, HCPower>() {

    override fun HCPower.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String? {
        val unit = config.getUnitOrNull<Power>() ?: return null
        return when(unit) {
            Power.KILOCALORIES -> {
                "${inKilocaloriesPerDay.format(1)}${context.getString(R.string.unit_type_power_kilocalories_suffix)}"
            }
            Power.WATTS -> {
                "${inWatts.format(1)}${context.getString(R.string.unit_type_power_watts_suffix)}"
            }
        }
    }

    override fun PowerRecord.getTime() = endTime

}