package com.kieronquinn.app.smartspacer.plugin.healthconnect.complications

import androidx.health.connect.client.records.HydrationRecord
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.DataType

class HydrationComplication: HealthConnectComplication<HydrationRecord>(
    DataType.HYDRATION
) {

    override val authority = "${BuildConfig.APPLICATION_ID}.complications.hydration"

}