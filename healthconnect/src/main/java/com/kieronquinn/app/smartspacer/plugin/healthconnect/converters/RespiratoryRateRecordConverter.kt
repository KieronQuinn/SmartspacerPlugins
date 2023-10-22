package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.RespiratoryRateRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication

object RespiratoryRateRecordConverter: DataTypeConverter<RespiratoryRateRecord, RespiratoryRateRecord>() {

    override fun RespiratoryRateRecord.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        return rate.toInt().toString()
    }

    override fun RespiratoryRateRecord.getTime() = time

}