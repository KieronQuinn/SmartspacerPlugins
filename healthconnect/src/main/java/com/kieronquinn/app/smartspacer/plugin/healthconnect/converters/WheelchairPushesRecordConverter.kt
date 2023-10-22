package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.WheelchairPushesRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication

object WheelchairPushesRecordConverter: DataTypeConverter<WheelchairPushesRecord, Long>() {

    override fun Long.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        return toString()
    }

    override fun WheelchairPushesRecord.getTime() = endTime

}