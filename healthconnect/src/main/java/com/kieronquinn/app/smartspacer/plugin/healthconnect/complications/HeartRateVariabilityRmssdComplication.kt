package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class HeartRateVariabilityRmssdComplication: HealthConnectComplication<HeartRateVariabilityRmssdRecord>(
    DataType.HEART_RATE_VARIABILITY
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.heartratevariabilityrmssd"

}