package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.FloorsClimbedRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class FloorsClimbedComplication: HealthConnectComplication<FloorsClimbedRecord>(
    DataType.FLOORS_CLIMBED
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.floorsclimbed"

}