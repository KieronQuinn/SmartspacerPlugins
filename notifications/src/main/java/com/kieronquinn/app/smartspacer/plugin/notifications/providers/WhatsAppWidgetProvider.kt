package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.WhatsAppComplication.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

class WhatsAppWidgetProvider: SmartspacerWidgetProvider() {

    companion object {
        const val PACKAGE_NAME_V2 = "com.whatsapp.v2"
        private const val IDENTIFIER_SUBTITLE = "${PACKAGE_NAME}:id/subtitle"

        private val COMPONENT_WIDGET = ComponentName(
            PACKAGE_NAME, "com.whatsapp.appwidget.WidgetProvider"
        )

        fun getAppWidgetProviderInfo(context: Context): AppWidgetProviderInfo? {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return appWidgetManager.installedProviders.firstOrNull {
                it.provider == COMPONENT_WIDGET
            }
        }
    }

    private val databaseRepository by inject<DatabaseRepository>()

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getAppWidgetProviderInfo(provideContext())
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val views = remoteViews?.load() ?: run {
            databaseRepository.setBadgeCount(PACKAGE_NAME_V2, 0)
            return
        }
        val unreadCount = views.findViewByIdentifier<TextView>(IDENTIFIER_SUBTITLE)
            ?.takeIf { it.isVisible }?.text?.toString()?.getUnreadCount() ?: 0
        databaseRepository.setBadgeCount(PACKAGE_NAME_V2, unreadCount)
    }

    private fun String.getUnreadCount(): Int? {
        return filter { it.isDigit() }.toIntOrNull()
    }

}