package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.Vo2MaxRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class Vo2MaxComplication: HealthConnectComplication<Vo2MaxRecord>(
    DataType.VO2_MAX
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.vo2max"

}