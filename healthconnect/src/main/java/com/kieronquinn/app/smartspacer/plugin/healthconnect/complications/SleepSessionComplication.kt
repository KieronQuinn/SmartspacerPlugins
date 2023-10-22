package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.SleepSessionRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class SleepSessionComplication: HealthConnectComplication<SleepSessionRecord>(
    DataType.SLEEP
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.sleep"

}