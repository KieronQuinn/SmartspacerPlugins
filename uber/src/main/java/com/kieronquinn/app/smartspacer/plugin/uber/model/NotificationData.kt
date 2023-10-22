package com.kieronquinn.app.smartspacer.plugin.uber.model

import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification

data class NotificationData(
    val notification: StatusBarNotification,
    val icon: Icon,
    val title: String,
    val subtitle: String,
    val progress: Float,
    val progressCar: Bitmap,
    val driver: Bitmap,
    val car: Bitmap,
    val expandedTitle: String,
    val expandedSubtitle: String
)