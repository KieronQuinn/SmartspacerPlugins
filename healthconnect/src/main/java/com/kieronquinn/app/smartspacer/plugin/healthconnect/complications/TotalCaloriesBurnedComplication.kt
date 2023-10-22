package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class TotalCaloriesBurnedComplication: HealthConnectComplication<TotalCaloriesBurnedRecord>(
    DataType.TOTAL_CALORIES
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.totalcaloriesburned"

}