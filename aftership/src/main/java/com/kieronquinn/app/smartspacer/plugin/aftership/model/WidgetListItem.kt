package com.kieronquinn.app.smartspacer.plugin.aftership.model

import android.graphics.Bitmap
import android.os.Bundle

data class WidgetListItem(
    val title: String,
    val courier: String,
    val state: String,
    val icon: Bitmap,
    val image: Bitmap?,
    val bundle: Bundle
)