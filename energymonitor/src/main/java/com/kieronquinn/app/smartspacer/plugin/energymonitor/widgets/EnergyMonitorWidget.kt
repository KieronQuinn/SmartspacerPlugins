package com.kieronquinn.app.smartspacer.plugin.energymonitor.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.VectorDrawable
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.TextView
import androidx.core.graphics.drawable.toBitmap
import com.kieronquinn.app.smartspacer.plugin.energymonitor.EnergyMonitorPlugin.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.energymonitor.repositories.StateRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.dp
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerWidgetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.findViewByIdentifier
import com.kieronquinn.app.smartspacer.sdk.utils.getResourceForIdentifier
import org.koin.android.ext.android.inject

class EnergyMonitorWidget: SmartspacerWidgetProvider() {

    companion object {
        private const val PROVIDER_CLASS =
            "com.watchandnavy.sw.ion.ui_v2.widget.single_device_status.DeviceBatteryStateWidgetProvider"
        private const val IDENTIFIER_BATTERY_LEVEL = "$PACKAGE_NAME:id/batteryLevelLabel"
        private const val IDENTIFIER_DEVICE_NAME = "$PACKAGE_NAME:id/deviceName"
        private const val IDENTIFIER_ICON = "$PACKAGE_NAME:id/deviceIcon"
        private const val IDENTIFIER_STATS = "$PACKAGE_NAME:id/statsLayout"
        private const val IDENTIFIER_STATUS_LABEL = "$PACKAGE_NAME:id/statusLabel"
        private const val IDENTIFIER_CHARGING = "$PACKAGE_NAME:string/charging"
        private val ICON_SIZE = 48.dp

        fun getProvider(context: Context): AppWidgetProviderInfo? {
            val manager = context.getSystemService(Context.APPWIDGET_SERVICE) as AppWidgetManager
            return manager.installedProviders.firstOrNull {
                it.provider.packageName == PACKAGE_NAME && it.provider.className == PROVIDER_CLASS
            }
        }

        fun clickWidget(context: Context, smartspacerId: String) {
            clickView(context, smartspacerId, IDENTIFIER_STATS)
        }
    }

    private val stateRepository by inject<StateRepository>()

    override fun getAppWidgetProviderInfo(smartspacerId: String): AppWidgetProviderInfo? {
        return getProvider(provideContext())
    }

    override fun onWidgetChanged(smartspacerId: String, remoteViews: RemoteViews?) {
        val views = remoteViews?.load() ?: return
        val batteryLevel = views.findViewByIdentifier<TextView>(IDENTIFIER_BATTERY_LEVEL)
            ?.text?.toString() ?: return
        val name = views.findViewByIdentifier<TextView>(IDENTIFIER_DEVICE_NAME)?.text?.toString()
            ?: return
        val icon = views.findViewByIdentifier<ImageView>(IDENTIFIER_ICON)?.getImageAsBitmap()
            ?: return
        val statusLabel = views.findViewByIdentifier<TextView>(IDENTIFIER_STATUS_LABEL)
            ?.text?.toString()
        val chargingLabel = views.context.getChargingLabel()
        val isCharging = statusLabel != null && chargingLabel != null &&
                statusLabel == chargingLabel
        stateRepository.setState(
            smartspacerId,
            StateRepository.State(batteryLevel, name, isCharging, icon)
        )
    }

    private fun Context.getChargingLabel(): String? {
        return getResourceForIdentifier(IDENTIFIER_CHARGING)?.let {
            getString(it)
        }
    }

    override fun getConfig(smartspacerId: String): Config {
        return Config()
    }

    private fun ImageView.getImageAsBitmap(): Bitmap? {
        return (drawable as? VectorDrawable)?.toBitmap(ICON_SIZE, ICON_SIZE)
    }

}