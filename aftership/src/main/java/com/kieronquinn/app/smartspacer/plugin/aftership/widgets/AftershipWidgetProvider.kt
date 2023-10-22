package com.kieronquinn.app.smartspacer.plugin.aftership.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmapOrNull
import com.kieronquinn.app.smartspacer.plugin.aftership.AftershipPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.aftership.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.aftership.model.WidgetListItem
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.AftershipRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.RemoteAdapter
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

class AftershipWidgetProvider: SmartspacerWidgetProvider() {

    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.widget.aftership"

        private val COMPONENT_WIDGET = ComponentName(
            PACKAGE_NAME,
            "com.aftership.shopper.views.widget.shipment.ShipmentAppWidgetProvider"
        )

        private const val IDENTIFIER_LIST = "$PACKAGE_NAME:id/list_view"
        private const val IDENTIFIER_TITLE = "$PACKAGE_NAME:id/shipment_title_tv"
        private const val IDENTIFIER_COURIER = "$PACKAGE_NAME:id/shipment_name_tv"
        private const val IDENTIFIER_STATE = "$PACKAGE_NAME:id/shipment_state_tv"
        private const val IDENTIFIER_ICON = "$PACKAGE_NAME:id/shipment_state_iv"
        private const val IDENTIFIER_IMAGE = "$PACKAGE_NAME:id/order_image_iv"

        fun getProvider(context: Context): AppWidgetProviderInfo? {
            return AppWidgetManager.getInstance(context).installedProviders.firstOrNull {
                it.provider == COMPONENT_WIDGET
            }
        }
    }

    private val aftershipRepository by inject<AftershipRepository>()

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext())
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        getAdapter(smartspacerId, IDENTIFIER_LIST)
    }

    override fun onAdapterConnected(smartspacerId: String, adapter: RemoteAdapter) {
        super.onAdapterConnected(smartspacerId, adapter)
        loadItems(adapter)
    }

    private fun loadItems(adapter: RemoteAdapter) {
        val items = ArrayList<WidgetListItem>()
        for(i in 0 until adapter.getCount()) {
            val item = adapter.getViewAt(i) ?: continue
            val extra = item.onClickResponses.firstOrNull()?.response?.fillInIntent?.extras
                ?: continue
            val view = item.remoteViews.load() ?: continue
            val title = view.findViewByIdentifier<TextView>(IDENTIFIER_TITLE)?.text ?: continue
            val courier = view.findViewByIdentifier<TextView>(IDENTIFIER_COURIER)?.text ?: continue
            val state = view.findViewByIdentifier<TextView>(IDENTIFIER_STATE)?.text ?: continue
            val icon = view.findViewByIdentifier<ImageView>(IDENTIFIER_ICON)?.getImageAsBitmap()
                ?: continue
            val image = view.findViewByIdentifier<ImageView>(IDENTIFIER_IMAGE)?.getImageAsBitmap()
            items.add(
                WidgetListItem(
                    title.toString(),
                    courier.toString(),
                    state.toString(),
                    icon,
                    image,
                    extra
                )
            )
        }
        if(items.isEmpty()) return //Assume this is a failed load rather than an actual empty list
        aftershipRepository.loadAdapterItems(items)
    }

    private fun ImageView.getImageAsBitmap(): Bitmap? {
        return drawable?.toBitmapOrNull()
    }

}