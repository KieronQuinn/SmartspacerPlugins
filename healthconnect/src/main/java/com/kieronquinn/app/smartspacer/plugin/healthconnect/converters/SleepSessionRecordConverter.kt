package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.SleepSessionRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication
import java.time.Duration

object SleepSessionRecordConverter: DataTypeConverter<SleepSessionRecord, Duration>() {

    override fun Duration.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        return context.getString(
            R.string.unit_type_duration_suffix,
            toHoursPart(),
            toMinutesPart()
        )
    }

    override fun SleepSessionRecord.getTime() = endTime

}