package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.StepsRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class StepsComplication: HealthConnectComplication<StepsRecord>(
    DataType.STEPS
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.steps"

}