package com.kieronquinn.app.smartspacer.plugin.googlemaps.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.ViewFlipper
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import com.kieronquinn.app.smartspacer.plugin.googlemaps.R
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import org.koin.android.ext.android.inject

class GoogleMapsTrafficWidget: SmartspacerWidgetProvider() {

    companion object {

        const val PACKAGE_NAME = "com.google.android.apps.maps"

        private const val COMPONENT_TRAFFIC =
            "com.google.android.apps.gmm.widget.traffic.TrafficWidgetProvider"

        private const val IDENTIFIER_PERMISSION_CONTAINER =
            "$PACKAGE_NAME:id/traffic_widget_permissions_container"

        private const val IDENTIFIER_VIEW_FLIPPER =
            "$PACKAGE_NAME:id/traffic_widget_map_view_flipper"

        private const val IDENTIFIER_IMAGE_1 =
            "$PACKAGE_NAME:id/traffic_widget_map_image_1"

        private const val IDENTIFIER_IMAGE_2 =
            "$PACKAGE_NAME:id/traffic_widget_map_image_2"

        fun getProviderInfo(context: Context): AppWidgetProviderInfo? {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            return appWidgetManager.installedProviders.firstOrNull {
                val provider = it.provider
                provider.packageName == PACKAGE_NAME && provider.className == COMPONENT_TRAFFIC
            }
        }

        fun clickPermissionsContainer(context: Context, smartspacerId: String) {
            clickView(context, smartspacerId, IDENTIFIER_PERMISSION_CONTAINER)
        }
    }

    private val googleMapsRepository by inject<GoogleMapsRepository>()

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProviderInfo(provideContext())
    }

    override fun getConfig(smartspacerId: String): Config {
        val resources = provideContext().resources
        return Config(
            width = resources.getDimensionPixelSize(R.dimen.widget_google_maps_traffic_width),
            height = resources.getDimensionPixelSize(R.dimen.widget_google_maps_traffic_height)
        )
    }

    override fun onWidgetChanged(
        smartspacerId: String,
        remoteViews: RemoteViews?
    ) {
        val views = remoteViews?.load() ?: return
        val permissionContainer =
            views.findViewByIdentifier<FrameLayout>(IDENTIFIER_PERMISSION_CONTAINER)
        if(permissionContainer?.isVisible == true) {
            //User needs to grant background location permission
            googleMapsRepository
                .updateTrafficState(false, null, null)
        }else{
            val images = views.loadImages()
            if(images != null) {
                googleMapsRepository
                    .updateTrafficState(true, images.first, images.second)
            }else{
                googleMapsRepository.clearTrafficImage()
            }
        }
    }

    private fun View.loadImages(): Pair<Bitmap, Bitmap>? {
        val viewFlipper = findViewByIdentifier<ViewFlipper>(IDENTIFIER_VIEW_FLIPPER) ?: return null
        viewFlipper.displayedChild = 0
        val zoomedIn = getMapBitmap(IDENTIFIER_IMAGE_1) ?: return null
        viewFlipper.displayedChild = 1
        val zoomedOut = getMapBitmap(IDENTIFIER_IMAGE_2) ?: return null
        return Pair(zoomedIn, zoomedOut)
    }

    private fun View.getMapBitmap(identifier: String): Bitmap? {
        val view = findViewByIdentifier<ImageView>(identifier) ?: return null
        return view.drawable.toBitmap()
    }

}