package com.kieronquinn.app.smartspacer.plugins.pokemongo.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.widget.RemoteViews
import android.widget.TextView
import com.kieronquinn.app.smartspacer.plugins.pokemongo.R
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository.WidgetConfiguration
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

abstract class DistanceWidgetProvider: BaseWidgetProvider() {

    companion object {
        private const val CLASS_WIDGET =
            "com.nianticproject.holoholo.libholoholo.appwidget.ActivitySummaryWidget"
        private const val IDENTIFIER_DISTANCE = ":id/weekly_distance_progress"
    }

    private val widgetRepository by inject<WidgetRepository>()

    private val appWidgetManager by lazy {
        provideContext().getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
    }

    private val providerInfo by lazy {
        appWidgetManager.installedProviders.firstOrNull {
            it.provider.packageName == variant.packageName &&
                    it.provider.className == CLASS_WIDGET
        }
    }

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return providerInfo
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val views = remoteViews?.load() ?: return
        val distance = views.findViewByIdentifier<TextView>(getIdentifier(IDENTIFIER_DISTANCE))
            ?.text?.toString() ?: return
        val distanceWithKm = provideContext()
            .getString(R.string.complication_distance_km, distance)
        val configuration = WidgetConfiguration(distanceWithKm, null)
        widgetRepository.writeDistanceConfiguration(variant, configuration)
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

}