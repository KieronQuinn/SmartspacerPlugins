package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.RestingHeartRateRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication

object RestingHeartRateRecordConverter: DataTypeConverter<RestingHeartRateRecord, Long>() {

    override fun Long.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        return toString()
    }

    override fun RestingHeartRateRecord.getTime() = time

}