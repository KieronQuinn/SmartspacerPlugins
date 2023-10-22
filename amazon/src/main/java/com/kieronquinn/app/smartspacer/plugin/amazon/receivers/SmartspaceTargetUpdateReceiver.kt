package com.kieronquinn.app.smartspacer.plugin.amazon.receivers

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.targets.AmazonTarget
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerTargetUpdateReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SmartspaceTargetUpdateReceiver: SmartspacerTargetUpdateReceiver(), KoinComponent {

    private val amazonRepository by inject<AmazonRepository>()

    override fun onRequestSmartspaceTargetUpdate(
        context: Context,
        requestTargets: List<RequestTarget>
    ) {
        requestTargets.forEach {
            when(it.authority){
                AmazonTarget.AUTHORITY -> {
                    amazonRepository.syncDeliveries(false)
                }
            }
        }
    }
}