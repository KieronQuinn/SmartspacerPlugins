package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.verifySecurity
import com.kieronquinn.app.smartspacer.plugins.sunrisesunset.repositories.SunriseSunsetRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SunsetRefreshReceiver: BroadcastReceiver(), KoinComponent {

    private val sunriseSunsetRepository by inject<SunriseSunsetRepository>()

    override fun onReceive(context: Context, intent: Intent) {
        intent.verifySecurity(context)
        sunriseSunsetRepository.calculateSunset()
    }

}