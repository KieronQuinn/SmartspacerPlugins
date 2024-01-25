package com.kieronquinn.app.smartspacer.plugin.tasker.utils.extensions

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isPackageInstalled

fun Context.resolveAppWidget(componentName: ComponentName): AppWidgetProviderInfo? {
    val appWidgetManager = AppWidgetManager.getInstance(this)
    return appWidgetManager.installedProviders.firstOrNull {
        it.provider == componentName
    }
}

private val PACKAGE_NAMES_COMPATIBLE = setOf(
    "net.dinglisch.android.taskerm", //Play Store version
    "net.dinglisch.android.tasker", //Direct Purchase version
    "com.llamalab.automate", //Automate, supports Tasker plugins
    "com.arlosoft.macrodroid" //Macrodroid, supports Tasker plugins
)

fun Context.isTaskerInstalled(): Boolean {
    return PACKAGE_NAMES_COMPATIBLE.any { packageManager.isPackageInstalled(it) }
}