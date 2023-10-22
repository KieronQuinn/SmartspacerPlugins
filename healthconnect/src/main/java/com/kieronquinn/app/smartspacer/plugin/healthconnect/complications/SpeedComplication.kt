package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.SpeedRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class SpeedComplication: HealthConnectComplication<SpeedRecord>(
    DataType.SPEED
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.speed"

}