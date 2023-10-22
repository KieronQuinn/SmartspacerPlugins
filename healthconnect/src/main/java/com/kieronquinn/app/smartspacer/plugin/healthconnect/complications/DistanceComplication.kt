package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.DistanceRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class DistanceComplication: HealthConnectComplication<DistanceRecord>(
    DataType.DISTANCE
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.distance"

}