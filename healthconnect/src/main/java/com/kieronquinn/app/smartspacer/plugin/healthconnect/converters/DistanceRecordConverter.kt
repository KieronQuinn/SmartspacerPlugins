package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.DistanceRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.Length
import androidx.health.connect.client.units.Length as HCLength

object DistanceRecordConverter: DataTypeConverter<DistanceRecord, HCLength>() {

    override fun HCLength.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String? {
        val unit = config.getUnitOrNull<Length>() ?: return null
        return when(unit) {
            Length.FEET -> {
                "${inFeet.toInt()}${context.getString(R.string.unit_type_length_feet_suffix)}"
            }
            Length.INCHES -> {
                "${inInches.toInt()}${context.getString(R.string.unit_type_length_inches_suffix)}"
            }
            Length.KILOMETERS -> {
                "${inKilometers.format(1)}${context.getString(R.string.unit_type_length_kilometers_suffix)}"
            }
            Length.METERS -> {
                "${inMeters.toInt()}${context.getString(R.string.unit_type_length_meters_suffix)}"
            }
            Length.MILES -> {
                "${inMiles.format(1)}${context.getString(R.string.unit_type_length_miles_suffix)}"
            }
        }
    }

    override fun DistanceRecord.getTime() = endTime

}