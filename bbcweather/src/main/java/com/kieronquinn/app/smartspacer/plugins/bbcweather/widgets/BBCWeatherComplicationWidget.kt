package com.kieronquinn.app.smartspacer.plugins.bbcweather.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugins.bbcweather.BBCWeatherPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugins.bbcweather.BuildConfig
import com.kieronquinn.app.smartspacer.plugins.bbcweather.repositories.BBCWeatherRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

class BBCWeatherComplicationWidget: SmartspacerWidgetProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.widgets.bbcweathercomplication"

        private const val IDENTIFIER_CLICKABLE = "$PACKAGE_NAME:id/widget_layout"
        private const val IDENTIFIER_ICON = "$PACKAGE_NAME:id/widget_weather_icon"
        private const val IDENTIFIER_TEMPERATURE_MAX = "$PACKAGE_NAME:id/widget_max_temperature"
        private const val IDENTIFIER_TEMPERATURE_MIN = "$PACKAGE_NAME:id/widget_min_temperature"

        private val COMPONENT_WIDGET = ComponentName(
            PACKAGE_NAME,
            "bbc.mobile.weather.feature.app.Widget_2x1"
        )

        fun getProvider(context: Context): AppWidgetProviderInfo? {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return appWidgetManager.installedProviders.firstOrNull {
                it.provider == COMPONENT_WIDGET
            }
        }

        fun clickWidget(context: Context, smartspacerId: String) {
            clickView(context, smartspacerId, IDENTIFIER_CLICKABLE)
        }
    }

    private val bbcWeatherRepository by inject<BBCWeatherRepository>()

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val views = remoteViews?.load() ?: return
        val iconImageView = views.findViewByIdentifier<ImageView>(IDENTIFIER_ICON) ?: return
        val icon = iconImageView.drawable.toBitmap()
        val iconContentDescription = iconImageView.contentDescription?.toString() ?: return
        val max = views.findViewByIdentifier<TextView>(IDENTIFIER_TEMPERATURE_MAX)?.text?.toString()
            ?: return
        val min = views.findViewByIdentifier<TextView>(IDENTIFIER_TEMPERATURE_MIN)?.text?.toString()
            ?: return
        //The "max" TextView is used as a label overnight, so only use if it's a temperature
        val temperature = max.takeIf { it.endsWith("°") } ?: min
        bbcWeatherRepository.setComplicationState(
            views.context,
            temperature,
            icon,
            iconContentDescription
        )
    }

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext())
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

}