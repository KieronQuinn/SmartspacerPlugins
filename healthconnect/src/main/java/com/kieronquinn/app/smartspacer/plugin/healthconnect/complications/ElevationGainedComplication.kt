package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.ElevationGainedRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class ElevationGainedComplication: HealthConnectComplication<ElevationGainedRecord>(
    DataType.ELEVATION
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.elevationgained"

}