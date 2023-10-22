package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.OxygenSaturationRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class OxygenSaturationComplication: HealthConnectComplication<OxygenSaturationRecord>(
    DataType.OXYGEN_SATURATION
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.oxygensaturation"

}