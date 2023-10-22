package com.kieronquinn.app.smartspacer.plugin.samsunghealth.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.widget.RemoteViews
import android.widget.TextView
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.SamsungHealthPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.complications.StepsComplication
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories.SamsungHealthSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.dp
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

class StepsWidgetProvider: SmartspacerWidgetProvider() {

    companion object {
        private const val PROVIDER_CLASS =
            "com.sec.android.app.shealth.widget.WalkMatePlainAppWidget"
        private const val IDENTIFIER_STEPS = "$PACKAGE_NAME:id/step_count"

        fun getProvider(context: Context): AppWidgetProviderInfo? {
            val manager = context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
            return manager.installedProviders.firstOrNull {
                it.provider.packageName == PACKAGE_NAME &&
                        it.provider.className == PROVIDER_CLASS
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
        val steps = views.findViewByIdentifier<TextView>(IDENTIFIER_STEPS)?.text ?: return
        settings.steps.setSync(steps.toString())
        SmartspacerComplicationProvider.notifyChange(provideContext(), StepsComplication::class.java)
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(
            width = WIDGET_WIDTH,
            height = WIDGET_HEIGHT
        )
    }

}