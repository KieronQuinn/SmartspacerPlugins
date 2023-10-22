package com.kieronquinn.app.smartspacer.plugin.healthconnect.utils.extensions

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import com.kieronquinn.app.smartspacer.plugin.healthconnect.notifications.NotificationChannel
import com.kieronquinn.app.smartspacer.plugin.healthconnect.notifications.NotificationId
import com.kieronquinn.app.smartspacer.plugin.healthconnect.notifications.createNotification

fun Context.showBatteryOptimisationNotification() {
    createNotification(NotificationChannel.ERRORS) {
        it.setSmallIcon(R.drawable.ic_health_connect_tinted)
        it.setContentTitle(getString(R.string.notification_battery_optimisation_title))
        it.setContentText(getString(R.string.notification_battery_optimisation_content))
        it.setContentIntent(
            android.app.PendingIntent.getActivity(
                this,
                NotificationId.ERRORS.ordinal,
                getBatteryOptimisationIntent(),
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
        )
        it.setAutoCancel(true)
        it.priority = NotificationCompat.PRIORITY_MAX
    }.also {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationId.ERRORS.ordinal, it)
    }
}