package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.RespiratoryRateRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class RespiratoryRateComplication: HealthConnectComplication<RespiratoryRateRecord>(
    DataType.RESPIRATORY_RATE
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.respiratoryrate"

}