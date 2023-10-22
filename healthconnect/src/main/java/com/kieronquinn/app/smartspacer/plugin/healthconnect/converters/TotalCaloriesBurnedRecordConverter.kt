package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.Energy
import androidx.health.connect.client.units.Energy as HCEnergy

object TotalCaloriesBurnedRecordConverter: DataTypeConverter<TotalCaloriesBurnedRecord, HCEnergy>() {

    override fun HCEnergy.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String? {
        val unit = config.getUnitOrNull<Energy>() ?: return null
        return when(unit) {
            Energy.CALORIES -> {
                "${inCalories.toInt()}${context.getString(R.string.unit_type_energy_calories_suffix)}"
            }
            Energy.JOULES -> {
                "${inJoules.toInt()}${context.getString(R.string.unit_type_energy_joules_suffix)}"
            }
            Energy.KILOCALORIES -> {
                "${inKilocalories.toInt()}${context.getString(R.string.unit_type_energy_kilocalories_suffix)}"
            }
            Energy.KILOJOULES -> {
                "${inKilojoules.toInt()}${context.getString(R.string.unit_type_energy_kilojoules_suffix)}"
            }
        }
    }

    override fun TotalCaloriesBurnedRecord.getTime() = endTime

}