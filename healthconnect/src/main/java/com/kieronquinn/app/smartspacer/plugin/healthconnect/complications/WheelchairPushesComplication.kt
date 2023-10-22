package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.WheelchairPushesRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class WheelchairPushesComplication: HealthConnectComplication<WheelchairPushesRecord>(
    DataType.WHEELCHAIR_PUSHES
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.wheelchairpushes"

}