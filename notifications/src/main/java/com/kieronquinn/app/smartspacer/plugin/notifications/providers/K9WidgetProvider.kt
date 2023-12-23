package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.K9Complication.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

class K9WidgetProvider: SmartspacerWidgetProvider() {

    companion object {
        private const val IDENTIFIER_UNREAD = "${PACKAGE_NAME}:id/unread_count"

        private val COMPONENT_WIDGET = ComponentName(
            PACKAGE_NAME, "com.fsck.k9.provider.UnreadWidgetProvider"
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
            databaseRepository.setBadgeCount(PACKAGE_NAME, 0)
            return
        }
        val unreadCount = views.findViewByIdentifier<TextView>(IDENTIFIER_UNREAD)
            ?.takeIf { it.isVisible }?.text?.toString()?.toIntOrNull() ?: 0
        databaseRepository.setBadgeCount(PACKAGE_NAME, unreadCount)
    }

}