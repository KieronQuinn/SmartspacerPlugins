package com.kieronquinn.app.smartspacer.plugin.aftership.receivers

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.AftershipRepository
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerTargetUpdateReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AftershipUpdateReceiver: SmartspacerTargetUpdateReceiver(), KoinComponent {

    private val aftershipRepository by inject<AftershipRepository>()

    override fun onRequestSmartspaceTargetUpdate(
        context: Context,
        requestTargets: List<RequestTarget>
    ) {
        aftershipRepository.updateAdapterItems()
    }

}