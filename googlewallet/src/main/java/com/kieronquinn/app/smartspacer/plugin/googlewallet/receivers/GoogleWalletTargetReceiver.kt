package com.kieronquinn.app.smartspacer.plugin.googlewallet.receivers

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.plugin.googlewallet.targets.GoogleWalletDynamicTarget
import com.kieronquinn.app.smartspacer.sdk.receivers.SmartspacerTargetUpdateReceiver
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class GoogleWalletTargetReceiver: SmartspacerTargetUpdateReceiver(), KoinComponent {

    private val googleWalletRepository by inject<GoogleWalletRepository>()

    override fun onRequestSmartspaceTargetUpdate(
        context: Context,
        requestTargets: List<RequestTarget>
    ) {
        val ids = requestTargets.filter {
            it.authority == GoogleWalletDynamicTarget.AUTHORITY
        }.map {
            it.smartspacerId
        }.toTypedArray()
        if(ids.isEmpty()) return
        googleWalletRepository.refreshDynamicTargets(ids)
    }

}