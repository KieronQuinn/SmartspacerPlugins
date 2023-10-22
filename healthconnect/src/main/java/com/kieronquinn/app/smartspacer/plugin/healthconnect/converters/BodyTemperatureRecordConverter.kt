package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.BodyTemperatureRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.Temperature

object BodyTemperatureRecordConverter: DataTypeConverter<BodyTemperatureRecord, BodyTemperatureRecord>() {

    override fun BodyTemperatureRecord.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String? {
        val unit = config.getUnitOrNull<Temperature>() ?: return null
        return when(unit) {
            Temperature.CELSIUS -> {
                "${temperature.inCelsius.format(1)}${context.getString(R.string.unit_type_blood_temperature_celsius_suffix)}"
            }
            Temperature.FAHRENHEIT -> {
                "${temperature.inCelsius.format(1)}${context.getString(R.string.unit_type_blood_temperature_fahrenheit_suffix)}"
            }
        }
    }

    override fun BodyTemperatureRecord.getTime() = time

}