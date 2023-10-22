package com.kieronquinn.app.smartspacer.plugin.uber.model

import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.service.notification.StatusBarNotification

data class TargetData(
    val notification: StatusBarNotification,
    val icon: Icon,
    val title: String,
    val subtitle: String,
    val image: Bitmap
)