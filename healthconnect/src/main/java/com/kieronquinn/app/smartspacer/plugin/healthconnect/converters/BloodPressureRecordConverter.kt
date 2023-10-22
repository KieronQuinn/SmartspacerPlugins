package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.BloodPressureRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication

object BloodPressureRecordConverter: DataTypeConverter<BloodPressureRecord, BloodPressureRecord>() {

    override fun BloodPressureRecord.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        return "${systolic.inMillimetersOfMercury.toInt()}/${diastolic.inMillimetersOfMercury.toInt()}${context.getString(R.string.unit_type_blood_pressure_suffix)}"
    }

    override fun BloodPressureRecord.getTime() = time

}