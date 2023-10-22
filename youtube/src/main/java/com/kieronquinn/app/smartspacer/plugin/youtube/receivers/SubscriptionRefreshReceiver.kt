package com.kieronquinn.app.smartspacer.plugin.youtube.receivers

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeRepository
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerComplicationUpdateReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SubscriptionRefreshReceiver: SmartspacerComplicationUpdateReceiver(), KoinComponent {

    private val youtubeRepository by inject<YouTubeRepository>()

    override fun onRequestSmartspaceComplicationUpdate(
        context: Context,
        requestComplications: List<RequestComplication>
    ) {
        requestComplications.forEach {
            youtubeRepository.updateSubscriberCount(it.smartspacerId)
        }
    }

}