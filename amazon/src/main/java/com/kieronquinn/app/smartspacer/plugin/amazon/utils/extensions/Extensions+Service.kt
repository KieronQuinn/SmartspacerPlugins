package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

import android.app.Notification
import android.app.Service
import android.content.pm.ServiceInfo
import android.os.Build
import com.kieronquinn.app.smartspacer.plugin.amazon.components.notifications.NotificationId

fun Service.startForegroundCompat(notificationId: NotificationId, notification: Notification) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                notificationId.ordinal,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        }else{
            startForeground(notificationId.ordinal, notification)
        }
    }catch (e: Exception) {
        //Caches ForegroundServiceStartNotAllowedException on S+ when unable to startForeground
    }
}