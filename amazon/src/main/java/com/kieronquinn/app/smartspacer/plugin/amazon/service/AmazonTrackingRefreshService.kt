package com.kieronquinn.app.smartspacer.plugin.amazon.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.webkit.CookieManager
import androidx.lifecycle.LifecycleService
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.components.notifications.NotificationChannel
import com.kieronquinn.app.smartspacer.plugin.amazon.components.notifications.NotificationId
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.NotificationRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.createWebView
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.startForegroundCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.whenCreated
import org.koin.android.ext.android.inject

class AmazonTrackingRefreshService: LifecycleService() {

    companion object {
        fun start(context: Context) {
            context.startService(Intent(context, AmazonTrackingRefreshService::class.java))
        }
    }

    private val amazonRepository by inject<AmazonRepository>()
    private val notifications by inject<NotificationRepository>()
    private val cookieManager by inject<CookieManager>()

    private val headlessOrderDetailsWebView by lazy {
        createWebView(this, cookieManager, amazonRepository.getUserAgent())
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundCompat(NotificationId.TRACKING_REFRESH, createNotification())
        whenCreated {
            amazonRepository.updateTrackingData(headlessOrderDetailsWebView)
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun createNotification(): Notification {
        return notifications.showNotification(
            NotificationId.TRACKING_REFRESH,
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
                    NotificationId.TRACKING_REFRESH.ordinal,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            it.setTicker(getString(R.string.notification_title_background_service))
        }
    }

}