package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.BodyTemperatureRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class BodyTemperatureComplication: HealthConnectComplication<BodyTemperatureRecord>(
    DataType.BODY_TEMPERATURE
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.bodytemperature"

}