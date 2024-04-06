package com.kieronquinn.app.smartspacer.plugin.controls.repositories

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.controls.components.notifications.NotificationChannel
import com.kieronquinn.app.smartspacer.plugin.controls.components.notifications.NotificationId
import com.kieronquinn.app.smartspacer.plugin.controls.components.notifications.createNotification

interface NotificationRepository {

    /**
     *  Shows a notification built with [builder] immediately
     */
    fun showNotification(
        id: NotificationId,
        channel: NotificationChannel,
        builder: (NotificationCompat.Builder) -> Unit
    ): Notification

    /**
     *  Cancels a previously shown notification with a given [id]
     */
    fun cancelNotification(id: NotificationId)

}

class NotificationRepositoryImpl(private val context: Context): NotificationRepository {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun showNotification(
        id: NotificationId,
        channel: NotificationChannel,
        builder: (NotificationCompat.Builder) -> Unit
    ): Notification {
        return context.createNotification(channel, builder).also {
            notificationManager.notify(id.ordinal, it)
        }
    }

    override fun cancelNotification(id: NotificationId) {
        return notificationManager.cancel(id.ordinal)
    }

}