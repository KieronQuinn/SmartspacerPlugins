package com.kieronquinn.app.smartspacer.plugins.bbcweather.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugins.bbcweather.BBCWeatherPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugins.bbcweather.BuildConfig
import com.kieronquinn.app.smartspacer.plugins.bbcweather.model.TargetState
import com.kieronquinn.app.smartspacer.plugins.bbcweather.repositories.BBCWeatherRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

class BBCWeatherTargetWidget: SmartspacerWidgetProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.widgets.bbcweathertarget"

        private const val IDENTIFIER_CLICKABLE = "$PACKAGE_NAME:id/location_name_and_date"
        private const val IDENTIFIER_ICON = "$PACKAGE_NAME:id/widget_weather_icon"
        private const val IDENTIFIER_LOCATION = "$PACKAGE_NAME:id/widget_location"
        private const val IDENTIFIER_TEMPERATURE_MAX = "$PACKAGE_NAME:id/widget_max_temperature"
        private const val IDENTIFIER_TEMPERATURE_MIN = "$PACKAGE_NAME:id/widget_min_temperature"

        private const val IDENTIFIER_TAB_DATE = "$PACKAGE_NAME:id/widget_day_tab%d_date"
        private const val IDENTIFIER_TAB_ICON = "$PACKAGE_NAME:id/widget_day_tab%d_icon"
        private const val IDENTIFIER_TAB_MAX = "$PACKAGE_NAME:id/widget_day_tab%d_max_temperature"
        private const val IDENTIFIER_TAB_MIN = "$PACKAGE_NAME:id/widget_day_tab%d_min_temperature"

        private val COMPONENT_WIDGET = ComponentName(
            PACKAGE_NAME,
            "bbc.mobile.weather.feature.app.Widget"
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
        val location = views.findViewByIdentifier<TextView>(IDENTIFIER_LOCATION)?.text?.toString()
            ?: return
        val iconImageView = views.findViewByIdentifier<ImageView>(IDENTIFIER_ICON) ?: return
        val icon = iconImageView.drawable.toBitmap()
        val iconContentDescription = iconImageView.contentDescription?.toString() ?: return
        val max = views.findViewByIdentifier<TextView>(IDENTIFIER_TEMPERATURE_MAX)?.text?.toString()
            ?: return
        val min = views.findViewByIdentifier<TextView>(IDENTIFIER_TEMPERATURE_MIN)?.text?.toString()
            ?: return
        //The "max" TextView is used as a label overnight, so only use if it's a temperature
        val temperature = max.takeIf { it.endsWith("°") } ?: min
        val items = ArrayList<TargetState.Item>()
        for(i in 1 .. 4) {
            views.loadTabItem(i)?.let { items.add(it) }
        }
        val state = TargetState(
            location,
            icon,
            iconContentDescription,
            temperature,
            items
        )
        bbcWeatherRepository.setTargetState(views.context, state)
    }

    private fun View.loadTabItem(index: Int): TargetState.Item? {
        val day = findViewByIdentifier<TextView>(String.format(IDENTIFIER_TAB_DATE, index))
            ?.text?.toString()
        val icon = findViewByIdentifier<ImageView>(String.format(IDENTIFIER_TAB_ICON, index))
        val max = findViewByIdentifier<TextView>(String.format(IDENTIFIER_TAB_MAX, index))
            ?.text?.toString()
        val min = findViewByIdentifier<TextView>(String.format(IDENTIFIER_TAB_MIN, index))
            ?.text?.toString()
        return TargetState.Item(
            day ?: return null,
            icon?.drawable?.toBitmap() ?: return null,
            icon.contentDescription?.toString() ?: return null,
            max ?: min ?: return null
        )
    }

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext())
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

}