package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.FloorsClimbedRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication

object FloorsClimbedRecordConverter: DataTypeConverter<FloorsClimbedRecord, Double>() {

    override fun Double.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        //Why is this even a double? Who counts 0.1 of a floor?
        return toInt().toString()
    }

    override fun FloorsClimbedRecord.getTime() = endTime

}