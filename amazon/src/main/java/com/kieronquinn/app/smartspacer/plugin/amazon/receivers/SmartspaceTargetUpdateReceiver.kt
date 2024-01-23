package com.kieronquinn.app.smartspacer.plugin.amazon.receivers

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.amazon.service.AmazonTrackingRefreshService
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerTargetUpdateReceiver
import org.koin.core.component.KoinComponent

class SmartspaceTargetUpdateReceiver: SmartspacerTargetUpdateReceiver(), KoinComponent {

    override fun onRequestSmartspaceTargetUpdate(
        context: Context,
        requestTargets: List<RequestTarget>
    ) {
        AmazonTrackingRefreshService.start(context)
    }
}