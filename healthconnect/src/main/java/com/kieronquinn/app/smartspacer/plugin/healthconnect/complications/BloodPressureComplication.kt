package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.BloodPressureRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class BloodPressureComplication: HealthConnectComplication<BloodPressureRecord>(
    DataType.BLOOD_PRESSURE
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.bloodpressure"

}