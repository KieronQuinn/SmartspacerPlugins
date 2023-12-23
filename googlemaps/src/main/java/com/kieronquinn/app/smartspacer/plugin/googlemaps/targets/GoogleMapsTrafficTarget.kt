package com.kieronquinn.app.smartspacer.plugin.googlemaps.targets

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.googlemaps.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.googlemaps.R
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository.TrafficLevel
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository.ZoomMode
import com.kieronquinn.app.smartspacer.plugin.googlemaps.ui.activities.ConfigurationActivity.NavGraphMapping
import com.kieronquinn.app.smartspacer.plugin.googlemaps.ui.activities.GoogleMapsTrafficTrampolineActivity.Companion.getIntent
import com.kieronquinn.app.smartspacer.plugin.googlemaps.widgets.GoogleMapsTrafficWidget
import com.kieronquinn.app.smartspacer.plugin.googlemaps.widgets.GoogleMapsTrafficWidget.Companion.PACKAGE_NAME
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.ui.activities.BaseConfigurationActivity
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.packageHasPermission
import com.kieronquinn.app.smartspacer.sdk.model.Backup
import com.kieronquinn.app.smartspacer.sdk.model.CompatibilityState
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.TapAction
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import android.graphics.drawable.Icon as AndroidIcon

class GoogleMapsTrafficTarget: SmartspacerTargetProvider() {

    private val googleMapsRepository by inject<GoogleMapsRepository>()
    private val dataRepository by inject<DataRepository>()

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        return listOfNotNull(loadTarget(smartspacerId))
    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = provideContext().getString(R.string.target_google_maps_title),
            description = provideContext().getString(R.string.target_google_maps_description),
            icon = AndroidIcon.createWithResource(provideContext(), R.drawable.ic_target_google_maps_traffic),
            widgetProvider = "${BuildConfig.APPLICATION_ID}.widget.googlemapstraffic",
            compatibilityState = getCompatibility(),
            configActivity = BaseConfigurationActivity.createIntent(
                provideContext(), NavGraphMapping.TARGET_GOOGLE_MAPS
            )
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        googleMapsRepository.clearTrafficImage(hasPermission = true, isLoading = true)
        notifyChange(smartspacerId)
        return true
    }

    override fun createBackup(smartspacerId: String): Backup {
        val settings = dataRepository.getTargetData(smartspacerId, TargetData::class.java)
            ?: return Backup()
        val gson = get<Gson>()
        return Backup(gson.toJson(settings))
    }

    override fun restoreBackup(smartspacerId: String, backup: Backup): Boolean {
        val gson = get<Gson>()
        val settings = try {
            gson.fromJson(backup.data ?: return false, TargetData::class.java)
        }catch (e: Exception){
            return false
        }
        dataRepository.updateTargetData(
            smartspacerId,
            TargetData::class.java,
            TargetData.TYPE,
            ::restoreNotifyChange
        ){
            TargetData(settings.mode, settings.minTrafficLevel)
        }
        return true
    }

    private fun restoreNotifyChange(context: Context, smartspacerId: String) {
        notifyChange(smartspacerId)
    }

    override fun onProviderRemoved(smartspacerId: String) {
        dataRepository.deleteTargetData(smartspacerId)
    }

    private fun loadTarget(id: String): SmartspaceTarget? {
        val traffic = googleMapsRepository.getTrafficState() ?: return null
        val settings = dataRepository.getTargetData(id, TargetData::class.java) ?: TargetData()
        if(!traffic.hasPermission && !hasPermission()) return getPermissionRequiredTarget(id)
        val zoomedOut = settings.mode == ZoomMode.OUT
        val trafficLevel = if(zoomedOut){
            traffic.trafficLevelZoomedOut
        }else{
            traffic.trafficLevelZoomedIn
        } ?: return null
        if(!trafficLevel.isAtLeast(settings.minTrafficLevel)) return null
        val bitmap = if(zoomedOut){
            traffic.zoomedOut
        }else{
            traffic.zoomedIn
        } ?: return null
        return getMapTarget(bitmap, trafficLevel)
    }

    private fun hasPermission(): Boolean {
        return provideContext().packageManager.packageHasPermission(
            PACKAGE_NAME,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
    }

    private fun getMapTarget(map: Bitmap, trafficLevel: TrafficLevel): SmartspaceTarget {
        return TargetTemplate.Image(
            context = provideContext(),
            id = "google_maps_traffic",
            componentName = ComponentName(provideContext(), GoogleMapsTrafficTarget::class.java),
            title = Text(provideContext().getString(trafficLevel.content)),
            subtitle = Text(provideContext().getString(R.string.target_google_maps_traffic_subtitle)),
            icon = Icon(AndroidIcon.createWithResource(
                provideContext(), R.drawable.ic_target_google_maps_traffic
            )),
            image = Icon(AndroidIcon.createWithBitmap(map), shouldTint = false),
            onClick = TapAction(intent = getIntent(provideContext()))
        ).create()
    }

    private fun getPermissionRequiredTarget(smartspacerId: String): SmartspaceTarget {
        return TargetTemplate.Basic(
            id = "google_maps_traffic",
            componentName = ComponentName(provideContext(), GoogleMapsTrafficTarget::class.java),
            title = Text(
                resources.getString(R.string.target_google_maps_traffic_permission_required_title)
            ),
            subtitle = Text(
                resources.getString(R.string.target_google_maps_traffic_permission_required_subtitle)
            ),
            icon = Icon(AndroidIcon.createWithResource(
                provideContext(), R.drawable.ic_target_google_maps_traffic
            )),
            onClick = TapAction(intent = getIntent(provideContext()))
        ).create()
    }

    private fun TrafficLevel.isAtLeast(other: TrafficLevel): Boolean {
        return ordinal >= other.ordinal
    }

    private fun getCompatibility(): CompatibilityState {
        return if(GoogleMapsTrafficWidget.getProviderInfo(provideContext()) != null){
            CompatibilityState.Compatible
        }else{
            val unsupported = provideContext().getString(
                R.string.target_google_maps_description_unsupported
            )
            CompatibilityState.Incompatible(unsupported)
        }
    }

    data class TargetData(
        val mode: ZoomMode = ZoomMode.IN,
        val minTrafficLevel: TrafficLevel = TrafficLevel.NO_TRAFFIC
    ) {
        companion object {
            const val TYPE = "maps"
        }
    }

}