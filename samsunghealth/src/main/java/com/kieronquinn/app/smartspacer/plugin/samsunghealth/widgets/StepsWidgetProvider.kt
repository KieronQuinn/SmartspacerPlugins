package com.kieronquinn.app.smartspacer.plugin.samsunghealth.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.TextView
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.SamsungHealthPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.complications.StepsComplication
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories.SamsungHealthSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.dp
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewsByType
import org.koin.android.ext.android.inject
import java.text.NumberFormat
import java.text.ParseException

class StepsWidgetProvider: SmartspacerWidgetProvider() {

    companion object {
        private const val PROVIDER_CLASS =
            "com.sec.android.app.shealth.widget.WalkMatePlainAppWidget"

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
        val views = remoteViews?.load() as? ViewGroup ?: return
        val steps = views.findViewsByType(TextView::class.java).firstNotNullOfOrNull {
            val text = it.text?.toString() ?: return@firstNotNullOfOrNull null
            text.takeIf { t -> t.isInt() }
        } ?: return
        settings.steps.setSync(steps)
        SmartspacerComplicationProvider.notifyChange(provideContext(), StepsComplication::class.java)
    }

    private fun String.isInt(): Boolean {
        return try {
            NumberFormat.getInstance().parse(this) != null
        } catch (e: ParseException) {
            false
        }
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(
            width = WIDGET_WIDTH,
            height = WIDGET_HEIGHT
        )
    }

}