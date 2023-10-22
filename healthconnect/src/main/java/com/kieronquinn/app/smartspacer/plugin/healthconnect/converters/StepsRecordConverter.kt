package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.StepsRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication

object StepsRecordConverter: DataTypeConverter<StepsRecord, Long>() {

    override fun Long.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        return toString()
    }

    override fun StepsRecord.getTime() = endTime

}