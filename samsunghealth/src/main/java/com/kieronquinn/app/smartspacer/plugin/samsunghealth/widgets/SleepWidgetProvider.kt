package com.kieronquinn.app.smartspacer.plugin.samsunghealth.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.view.children
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.SamsungHealthPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.complications.SleepComplication
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories.SamsungHealthSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.dp
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

class SleepWidgetProvider: SmartspacerWidgetProvider() {

    companion object {
        private const val IDENTIFIER_TIME_HOURS = "$PACKAGE_NAME:id/sleep_widget_hour_value"
        private const val PROVIDER_CLASS =
            "com.samsung.android.app.shealth.tracker.sleepWidget.SleepWidget"

        fun getProvider(context: Context): AppWidgetProviderInfo? {
            val manager = context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
            return manager.installedProviders.firstOrNull {
                it.provider.packageName == PACKAGE_NAME && it.provider.className == PROVIDER_CLASS
            }
        }

        private val WIDGET_WIDTH = 368.dp
        private val WIDGET_HEIGHT = 146.dp
    }

    private val settings by inject<SamsungHealthSettingsRepository>()

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext())
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val views = remoteViews?.load() ?: return
        val hours = views.findViewByIdentifier<TextView>(IDENTIFIER_TIME_HOURS) ?: return
        val container = hours.parent as LinearLayout
        val sleepTime = container.getSleepTime()
        //Only update if required, this also prevents resetting the time without changes
        val current = settings.sleepTime.getSync()
        if(sleepTime == current) return
        settings.sleepTime.setSync(sleepTime)
        settings.sleepTimestamp.setSync(System.currentTimeMillis())
        SmartspacerComplicationProvider.notifyChange(provideContext(), SleepComplication::class.java)
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(
            width = WIDGET_WIDTH,
            height = WIDGET_HEIGHT
        )
    }

    private fun LinearLayout.getSleepTime(): String {
        return children.filterIsInstance<TextView>().joinToString("") {
            it.text
        }
    }

}