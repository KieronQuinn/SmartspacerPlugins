package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.RestingHeartRateRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class RestingHeartRateComplication: HealthConnectComplication<RestingHeartRateRecord>(
    DataType.RESTING_HEART_RATE
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.restingheartrate"

}