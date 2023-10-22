package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.BloodGlucoseRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class BloodGlucoseComplication: HealthConnectComplication<BloodGlucoseRecord>(
    DataType.BLOOD_GLUCOSE
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.bloodglucose"

}