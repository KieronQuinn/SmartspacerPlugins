package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.OxygenSaturationRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication

object OxygenSaturationRecordConverter: DataTypeConverter<OxygenSaturationRecord, OxygenSaturationRecord>() {

    override fun OxygenSaturationRecord.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        return "${percentage.value}${context.getString(R.string.unit_type_percent_suffix)}"
    }

    override fun OxygenSaturationRecord.getTime() = time

}