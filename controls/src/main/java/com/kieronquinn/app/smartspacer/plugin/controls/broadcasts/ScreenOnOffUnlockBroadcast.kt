package com.kieronquinn.app.smartspacer.plugin.controls.broadcasts

import android.content.Intent
import android.content.IntentFilter
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerBroadcastProvider
import org.koin.android.ext.android.inject

class ScreenOnOffUnlockBroadcast: SmartspacerBroadcastProvider() {

    private val controlsRepository by inject<ControlsRepository>()

    override fun onReceive(intent: Intent) {
        //Trigger a listen for state changes to make sure a control is up to date
        controlsRepository.startListeningFromWake()
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(listOf(
            IntentFilter(Intent.ACTION_SCREEN_OFF),
            IntentFilter(Intent.ACTION_SCREEN_ON),
            IntentFilter(Intent.ACTION_USER_PRESENT),
        ))
    }

}