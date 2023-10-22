package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication

object HeartRateVariabilityRmssdRecordConverter: DataTypeConverter<HeartRateVariabilityRmssdRecord, HeartRateVariabilityRmssdRecord>() {

    override fun HeartRateVariabilityRmssdRecord.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        return heartRateVariabilityMillis.toInt().toString()
    }

    override fun HeartRateVariabilityRmssdRecord.getTime() = time

}