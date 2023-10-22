package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.Vo2MaxRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication

object Vo2MaxRecordConverter: DataTypeConverter<Vo2MaxRecord, Vo2MaxRecord>() {

    override fun Vo2MaxRecord.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String {
        return "${vo2MillilitersPerMinuteKilogram.toInt()}${context.getString(R.string.unit_type_volume_milliliters_suffix)}"
    }

    override fun Vo2MaxRecord.getTime() = time

}