package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context

fun Context.resolveAppWidget(componentName: ComponentName): AppWidgetProviderInfo? {
    val appWidgetManager = AppWidgetManager.getInstance(this)
    return appWidgetManager.installedProviders.firstOrNull {
        it.provider == componentName
    }
}