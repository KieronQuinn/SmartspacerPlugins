package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.util.SizeF
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.notifications.complications.GoogleVoiceComplication.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import com.kieronquinn.app.smartspacer.sdk.utils.getColumnSpan
import com.kieronquinn.app.smartspacer.sdk.utils.getRowSpan
import org.koin.android.ext.android.inject

class GoogleVoiceWidgetProvider: SmartspacerWidgetProvider() {

    companion object {
        private const val IDENTIFIER_MESSAGES_COUNT = "$PACKAGE_NAME:id/messages_new_count"
        private const val IDENTIFIER_CALLS_COUNT = "$PACKAGE_NAME:id/calls_new_count"
        private const val IDENTIFIER_VOICEMAILS_COUNT = "$PACKAGE_NAME:id/voicemails_new_count"

        private val COMPONENT_WIDGET = ComponentName(
            PACKAGE_NAME,
            "com.google.android.apps.voice.widget.LargeAppWidgetProvider"
        )

        fun getAppWidgetProviderInfo(context: Context): AppWidgetProviderInfo? {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return appWidgetManager.installedProviders.firstOrNull {
                it.provider == COMPONENT_WIDGET
            }
        }
    }

    private val databaseRepository by inject<DatabaseRepository>()

    private val width by lazy {
        getColumnSpan(4)
    }

    private val height by lazy {
        getRowSpan(3)
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val sizedRemoteViews = remoteViews?.let {
            getSizedRemoteView(remoteViews, SizeF(width.toFloat(), height.toFloat()))
        }
        val views = sizedRemoteViews?.load() ?: run {
            databaseRepository.setBadgeCount(PACKAGE_NAME, 0)
            return
        }
        val callsCount = views.findViewByIdentifier<TextView>(IDENTIFIER_CALLS_COUNT)
            ?.takeIf { it.isVisible }?.text?.toString()?.toIntOrNull() ?: 0
        val messagesCount = views.findViewByIdentifier<TextView>(IDENTIFIER_MESSAGES_COUNT)
            ?.takeIf { it.isVisible }?.text?.toString()?.toIntOrNull() ?: 0
        val voicemailsCount = views.findViewByIdentifier<TextView>(IDENTIFIER_VOICEMAILS_COUNT)
            ?.takeIf { it.isVisible }?.text?.toString()?.toIntOrNull() ?: 0
        databaseRepository.setBadgeCount(PACKAGE_NAME, callsCount + messagesCount + voicemailsCount)
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config(width, height)
    }

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getAppWidgetProviderInfo(provideContext())
    }

}