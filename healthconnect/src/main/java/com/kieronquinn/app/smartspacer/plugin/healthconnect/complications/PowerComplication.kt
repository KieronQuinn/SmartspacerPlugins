package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.PowerRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class PowerComplication: HealthConnectComplication<PowerRecord>(
    DataType.POWER
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.power"

}