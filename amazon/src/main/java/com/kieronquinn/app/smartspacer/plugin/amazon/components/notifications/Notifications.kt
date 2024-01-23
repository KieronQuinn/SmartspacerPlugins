package com.kieronquinn.app.smartspacer.plugin.amazon.components.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.amazon.R
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.hasNotificationPermission

fun Context.createNotification(
    channel: NotificationChannel,
    builder: (NotificationCompat.Builder) -> Unit
): Notification {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationChannel =
        android.app.NotificationChannel(
            channel.id,
            getString(channel.titleRes),
            channel.importance
        ).apply {
            description = getString(channel.descRes)
        }
    notificationManager.createNotificationChannel(notificationChannel)
    return NotificationCompat.Builder(this, channel.id).apply(builder).apply {
        val text = getContentText() ?: return@apply
        setStyle(NotificationCompat.BigTextStyle(this).bigText(text))
    }.build()
}

private fun NotificationCompat.Builder.getContentText(): CharSequence? {
    return this::class.java.getDeclaredField("mContentText").apply {
        isAccessible = true
    }.get(this) as CharSequence?
}

enum class NotificationChannel(
    val id: String,
    val importance: Int,
    val titleRes: Int,
    val descRes: Int
) {
    BACKGROUND_SERVICE(
        "background_service",
        NotificationManager.IMPORTANCE_DEFAULT,
        R.string.notification_channel_background_service_title,
        R.string.notification_channel_background_service_subtitle
    ),
    ERROR(
        "error",
        NotificationManager.IMPORTANCE_HIGH,
        R.string.notification_channel_error_title,
        R.string.notification_channel_error_subtitle
    ),
    ORDER_LINK(
        "order_link",
        NotificationManager.IMPORTANCE_HIGH,
        R.string.notification_channel_order_link_title,
        R.string.notification_channel_order_link_subtitle
    );

    fun isEnabled(context: Context): Boolean {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(!context.hasNotificationPermission()) return false
        if(!notificationManager.areNotificationsEnabled()) return false
        //If the channel hasn't been created yet, default to enabled
        val channel = notificationManager.getNotificationChannel(id) ?: return true
        return channel.importance != NotificationManager.IMPORTANCE_NONE
    }
}

enum class NotificationId {
    UNUSED,
    DELIVERY_REFRESH,
    TRACKING_REFRESH,
    ERROR,
    ORDER_LINK
}