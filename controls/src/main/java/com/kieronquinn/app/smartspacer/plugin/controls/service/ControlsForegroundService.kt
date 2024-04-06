package com.kieronquinn.app.smartspacer.plugin.controls.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.LifecycleService
import com.kieronquinn.app.smartspacer.plugin.controls.R
import com.kieronquinn.app.smartspacer.plugin.controls.components.notifications.NotificationChannel
import com.kieronquinn.app.smartspacer.plugin.controls.components.notifications.NotificationId
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.NotificationRepository
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.isServiceRunning
import org.koin.android.ext.android.inject

class ControlsForegroundService: LifecycleService() {

    companion object {
        fun startIfNeeded(context: Context) {
            if(!context.isServiceRunning(ControlsForegroundService::class.java)) {
                try {
                    context.startService(
                        Intent(context, ControlsForegroundService::class.java)
                    )
                }catch (e: Exception) {
                    //Can't start now, will try again later
                }
            }
        }
    }

    private val notifications by inject<NotificationRepository>()

    override fun onCreate() {
        super.onCreate()
        try {
            startForeground(NotificationId.BACKGROUND_SERVICE.ordinal, createNotification())
        }catch (e: Exception) {
            //Can't start now, will try again later
        }
    }

    private fun createNotification(): Notification {
        return notifications.showNotification(
            NotificationId.BACKGROUND_SERVICE,
            NotificationChannel.BACKGROUND_SERVICE
        ) {
            val notificationIntent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, NotificationChannel.BACKGROUND_SERVICE.id)
            }
            it.setContentTitle(getString(R.string.notification_title_background_service))
            it.setContentText(getString(R.string.notification_content_background_service))
            it.setSmallIcon(R.drawable.ic_notification)
            it.setOngoing(true)
            it.setContentIntent(
                PendingIntent.getActivity(
                    this,
                    NotificationId.BACKGROUND_SERVICE.ordinal,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            it.setTicker(getString(R.string.notification_title_background_service))
        }
    }

}