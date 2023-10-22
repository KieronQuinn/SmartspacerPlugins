package com.kieronquinn.app.smartspacer.plugin.healthconnect.receivers

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.healthconnect.service.HealthConnectForegroundService
import com.kieronquinn.app.smartspacer.plugin.healthconnect.service.HealthConnectForegroundService.UpdateItem
import com.kieronquinn.app.smartspacer.plugin.healthconnect.utils.extensions.hasDisabledBatteryOptimisation
import com.kieronquinn.app.smartspacer.plugin.healthconnect.utils.extensions.showBatteryOptimisationNotification
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerComplicationUpdateReceiver
import org.koin.core.component.KoinComponent

class HealthConnectReceiver: SmartspacerComplicationUpdateReceiver(), KoinComponent {

    override fun onRequestSmartspaceComplicationUpdate(
        context: Context,
        requestComplications: List<RequestComplication>
    ) {
        val intent = HealthConnectForegroundService.createIntent(
            context, requestComplications.map { UpdateItem(it.smartspacerId, it.authority) }
        )
        try {
            context.startForegroundService(intent)
        }catch (e: Exception) {
            if(!context.hasDisabledBatteryOptimisation()) {
                context.showBatteryOptimisationNotification()
            }
        }
    }

}