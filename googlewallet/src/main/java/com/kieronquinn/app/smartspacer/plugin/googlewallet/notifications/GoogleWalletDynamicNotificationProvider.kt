package com.kieronquinn.app.smartspacer.plugin.googlewallet.notifications

import android.service.notification.StatusBarNotification
import com.kieronquinn.app.smartspacer.plugin.googlewallet.PACKAGE_GMS
import com.kieronquinn.app.smartspacer.plugin.googlewallet.repositories.GoogleWalletRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerNotificationProvider
import org.koin.android.ext.android.inject

class GoogleWalletDynamicNotificationProvider: SmartspacerNotificationProvider() {

    companion object {
        private const val CHANNEL_ID_PASSES = "wallet.passes"
        private const val EXTRA_NOTIFICATION_ID = "pass_notification_id_ext"
    }

    private val googleWalletRepository by inject<GoogleWalletRepository>()

    override fun onNotificationsChanged(
        smartspacerId: String,
        isListenerEnabled: Boolean,
        notifications: List<StatusBarNotification>
    ) {
        val hasPassNotification = notifications.any {
            it.notification.channelId == CHANNEL_ID_PASSES
                    && it.notification.extras.containsKey(EXTRA_NOTIFICATION_ID)
        }
        if(!hasPassNotification) return
        googleWalletRepository.onPassNotificationReceived()
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(setOf(PACKAGE_GMS))
    }

}