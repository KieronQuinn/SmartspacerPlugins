package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.SpeedRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.Velocity

object SpeedRecordConverter: DataTypeConverter<SpeedRecord, SpeedRecord>() {

    override fun SpeedRecord.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String? {
        val newestSpeed = samples.maxByOrNull { it.time }?.speed ?: return null
        val unit = config.getUnitOrNull<Velocity>() ?: return null
        return when(unit) {
            Velocity.MILES_PER_HOUR -> {
                "${newestSpeed.inMilesPerHour.format()}${context.getString(R.string.unit_type_velocity_miles_per_hour_suffix)}"
            }
            Velocity.METERS_PER_SECOND -> {
                "${newestSpeed.inMetersPerSecond.format()}${context.getString(R.string.unit_type_velocity_meters_per_second_suffix)}"
            }
            Velocity.KILOMETERS_PER_HOUR -> {
                "${newestSpeed.inKilometersPerHour.format()}${context.getString(R.string.unit_type_velocity_kilometers_per_hour_suffix)}"
            }
        }
    }

    override fun SpeedRecord.getTime() = endTime

}