package com.kieronquinn.app.smartspacer.plugin.notifications.providers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.TelegramRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.RemoteAdapter
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

class TelegramWidgetProvider: SmartspacerWidgetProvider() {


    private val telegramRepository by inject<TelegramRepository>()

    private val packageName = telegramRepository.getTelegramPackageName()
    private val classWidget = "$packageName.ChatsWidgetProvider"
    private val identifierListView = "$packageName:id/list_view"
    private val identifierBadge = "$packageName:id/shortcut_widget_item_badge"

    private val databaseRepository by inject<DatabaseRepository>()

    private val appWidgetManager by lazy {
        provideContext().getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
    }

    private val providerInfo by lazy {
        appWidgetManager.installedProviders.firstOrNull {
            it.provider.packageName == packageName &&
                    it.provider.className == classWidget
        }
    }

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return providerInfo
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        getAdapter(smartspacerId, identifierListView)
    }

    override fun onViewDataChanged(smartspacerId: String, viewIdentifier: String?, viewId: Int?) {
        super.onViewDataChanged(smartspacerId, viewIdentifier, viewId)
        if(viewIdentifier == identifierListView) {
            getAdapter(smartspacerId, viewIdentifier)
        }
    }

    override fun onAdapterConnected(smartspacerId: String, adapter: RemoteAdapter) {
        var count = 0
        for(i in 0 until adapter.getCount()) {
            val item = adapter.getViewAt(i)
            count += item?.remoteViews?.load()?.getBadgeCount() ?: 0
        }
        databaseRepository.setBadgeCount(packageName, count)
    }

    private fun View.getBadgeCount(): Int {
        if (this !is ViewGroup) return 0
        val badge = findViewByIdentifier<TextView>(identifierBadge)
        val count = badge?.let {
            //Invisible = Read (even though it keeps the last count???)
            if(!it.isVisible) return@let null
            //Disabled = muted
            if(!it.isEnabled) return@let null
            it.text.toString().toIntOrNull()
        } ?: 0
        return count
    }

}