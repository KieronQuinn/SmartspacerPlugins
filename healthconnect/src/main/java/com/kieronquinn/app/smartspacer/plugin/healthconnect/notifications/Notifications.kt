package com.kieronquinn.app.smartspacer.plugin.healthconnect.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.kieronquinn.app.smartspacer.plugin.healthconnect.R
import android.app.NotificationChannel as AndroidNotificationChannel

fun Context.createNotification(
    channel: NotificationChannel,
    builder: (NotificationCompat.Builder) -> Unit
): Notification {
    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val notificationChannel =
        AndroidNotificationChannel(
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
    ERRORS(
        "errors",
        NotificationManager.IMPORTANCE_MAX,
        R.string.notification_channel_errors_title,
        R.string.notification_channel_errors_subtitle
    )
}

enum class NotificationId {
    UNUSED,
    BACKGROUND_SERVICE,
    ERRORS
}