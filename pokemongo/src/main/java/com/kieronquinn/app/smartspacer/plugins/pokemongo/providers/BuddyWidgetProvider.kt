package com.kieronquinn.app.smartspacer.plugins.pokemongo.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RemoteViews
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

abstract class BuddyWidgetProvider: BaseWidgetProvider() {

    companion object {
        private const val CLASS_WIDGET =
            "com.nianticproject.holoholo.libholoholo.appwidget.BuddyWidget"
        private const val IDENTIFIER_NO_BUDDY = ":id/no_buddy_view"
        private const val IDENTIFIER_BUDDY_STATS = ":id/buddy_stats_view"
        private const val IDENTIFIER_IMAGE = ":id/buddy_image"
        private const val IDENTIFIER_STATS = ":id/buddy_stats"
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
        val configuration = when {
            views.isVisible(IDENTIFIER_NO_BUDDY) -> null
            views.isVisible(IDENTIFIER_BUDDY_STATS) -> {
                val progress = views.findViewByIdentifier<LinearLayout>(
                    getIdentifier(IDENTIFIER_STATS)
                )?.child()?.child()?.child()?.getProgressText()
                val image = views.findViewByIdentifier<ImageView>(getIdentifier(IDENTIFIER_IMAGE))
                    ?.getImageAsBitmap()
                progress?.let {
                    WidgetRepository.WidgetConfiguration(it, image)
                }
            }
            else -> null
        }
        widgetRepository.writeBuddyConfiguration(variant, configuration)
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

}