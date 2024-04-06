package com.kieronquinn.app.smartspacer.plugin.controls.receivers

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerVisibilityChangedReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SmartspaceVisibilityChangedReceiver: SmartspacerVisibilityChangedReceiver(), KoinComponent {

    private val controlsRepository by inject<ControlsRepository>()

    override fun onSmartspaceVisibilityChanged(context: Context, visible: Boolean, timestamp: Long) {
        if(visible) {
            controlsRepository.startListening()
        }else{
            controlsRepository.stopListening()
        }
    }

}