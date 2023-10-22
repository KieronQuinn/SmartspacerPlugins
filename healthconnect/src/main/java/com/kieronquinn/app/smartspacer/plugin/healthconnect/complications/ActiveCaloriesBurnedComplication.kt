package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class ActiveCaloriesBurnedComplication: HealthConnectComplication<ActiveCaloriesBurnedRecord>(
    DataType.ACTIVE_CALORIES_BURNED
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.activecaloriesburned"

}