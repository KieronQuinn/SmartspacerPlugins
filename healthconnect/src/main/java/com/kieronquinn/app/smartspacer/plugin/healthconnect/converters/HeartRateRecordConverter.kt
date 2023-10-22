package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.HeartRateRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication

object HeartRateRecordConverter: DataTypeConverter<HeartRateRecord, Long>() {

    override fun Long.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        return toString()
    }

    override fun HeartRateRecord.getTime() = endTime

}