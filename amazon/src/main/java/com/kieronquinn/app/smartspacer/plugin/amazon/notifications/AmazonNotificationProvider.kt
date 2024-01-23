package com.kieronquinn.app.smartspacer.plugin.amazon.notifications

import android.service.notification.StatusBarNotification
import com.kieronquinn.app.smartspacer.plugin.amazon.PACKAGE_NAME_GLOBAL
import com.kieronquinn.app.smartspacer.plugin.amazon.PACKAGE_NAME_INDIA
import com.kieronquinn.app.smartspacer.plugin.amazon.service.AmazonDeliveryRefreshService
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerNotificationProvider

class AmazonNotificationProvider: SmartspacerNotificationProvider() {

    override fun onNotificationsChanged(
        smartspacerId: String,
        isListenerEnabled: Boolean,
        notifications: List<StatusBarNotification>
    ) {
        if(notifications.isNotEmpty()){
            AmazonDeliveryRefreshService.start(provideContext())
        }
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(
            setOf(PACKAGE_NAME_GLOBAL, PACKAGE_NAME_INDIA, "com.matusmak.fakenotifications") //TODO remove
        )
    }

}