package com.kieronquinn.app.smartspacer.plugin.healthconnect.converters

import android.content.Context
import androidx.health.connect.client.records.HydrationRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.complications.HealthConnectComplication
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.Volume
import androidx.health.connect.client.units.Volume as HCVolume

object HydrationRecordConverter: DataTypeConverter<HydrationRecord, HCVolume>() {

    override fun HCVolume.format(
        context: Context,
        config: HealthConnectComplication.ComplicationData
    ): String? {
        val unit = config.getUnitOrNull<Volume>() ?: return null
        return when(unit) {
            Volume.LITERS -> {
                "${inLiters.format(1)}${context.getString(R.string.unit_type_volume_liters_suffix)}"
            }
            Volume.MILLILITERS -> {
                "${inMilliliters.toInt()}${context.getString(R.string.unit_type_volume_milliliters_suffix)}"
            }
            Volume.FLUID_OZ -> {
                "${inFluidOuncesUs.toInt()}${context.getString(R.string.unit_type_volume_fluid_oz_suffix)}"
            }
        }
    }

    override fun HydrationRecord.getTime() = endTime

}