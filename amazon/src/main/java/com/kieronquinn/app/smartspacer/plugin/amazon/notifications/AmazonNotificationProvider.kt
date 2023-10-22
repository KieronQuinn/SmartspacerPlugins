package com.kieronquinn.app.smartspacer.plugin.amazon.notifications

import android.service.notification.StatusBarNotification
import com.kieronquinn.app.smartspacer.plugin.amazon.AmazonPluginApplication.Companion.PACKAGE_NAME_GLOBAL
import com.kieronquinn.app.smartspacer.plugin.amazon.AmazonPluginApplication.Companion.PACKAGE_NAME_INDIA
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerNotificationProvider
import org.koin.android.ext.android.inject

class AmazonNotificationProvider: SmartspacerNotificationProvider() {

    private val amazonRepository by inject<AmazonRepository>()

    override fun onNotificationsChanged(
        smartspacerId: String,
        isListenerEnabled: Boolean,
        notifications: List<StatusBarNotification>
    ) {
        if(notifications.isNotEmpty()){
            //A state change from the server should trigger a full reload
            amazonRepository.syncDeliveries(true)
        }
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(
            setOf(PACKAGE_NAME_GLOBAL, PACKAGE_NAME_INDIA)
        )
    }

}