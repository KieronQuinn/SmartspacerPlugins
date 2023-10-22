package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.BloodGlucoseRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.Glucose

object BloodGlucoseRecordConverter: DataTypeConverter<BloodGlucoseRecord, BloodGlucoseRecord>() {

    override fun BloodGlucoseRecord.format(
        context: Context,
        config: ComplicationData
    ): String? {
        val unit = config.getUnitOrNull<Glucose>() ?: return null
        return when(unit) {
            Glucose.MMOL -> {
                "${level.inMillimolesPerLiter.format(1)}${context.getString(R.string.unit_type_blood_glucose_mmol_suffix)}"
            }
            Glucose.MGDL -> {
                "${level.inMilligramsPerDeciliter.toInt()}${context.getString(R.string.unit_type_blood_glucose_mgdl_suffix)}"
            }
        }
    }

    override fun BloodGlucoseRecord.getTime() = time

}