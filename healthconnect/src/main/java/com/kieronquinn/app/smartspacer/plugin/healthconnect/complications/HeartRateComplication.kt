package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.HeartRateRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class HeartRateComplication: HealthConnectComplication<HeartRateRecord>(DataType.HEART_RATE) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.heartrate"

}