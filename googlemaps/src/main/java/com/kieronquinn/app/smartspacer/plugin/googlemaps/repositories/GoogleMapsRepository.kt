package com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.googlemaps.R
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository.TrafficLevel
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository.TrafficState
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository.ZoomMode
import com.kieronquinn.app.smartspacer.plugin.googlemaps.targets.GoogleMapsTrafficTarget
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

interface GoogleMapsRepository {

    fun clearTrafficImage()
    fun updateTrafficState(hasPermission: Boolean, zoomedIn: Bitmap?, zoomedOut: Bitmap?)
    fun getTrafficState(): TrafficState?

    data class TrafficState(
        val hasPermission: Boolean,
        val zoomedIn: Bitmap?,
        val zoomedOut: Bitmap?,
        val trafficLevelZoomedIn: TrafficLevel?,
        val trafficLevelZoomedOut: TrafficLevel?
    )

    enum class TrafficLevel(
        @StringRes val content: Int,
        @StringRes val description: Int,
        vararg val color: Int
    ) {
        NO_TRAFFIC(
            R.string.target_google_maps_traffic_level_no,
            R.string.target_google_maps_traffic_setting_min_traffic_level_no,
            Color.parseColor("#63D668"),
            Color.parseColor("#388E3C")
        ),
        LIGHT_TRAFFIC(
            R.string.target_google_maps_traffic_level_light,
            R.string.target_google_maps_traffic_setting_min_traffic_level_light,
            Color.parseColor("#FF974D"),
            Color.parseColor("#F9A825")
        ),
        MID_TRAFFIC(
            R.string.target_google_maps_traffic_level_mid,
            R.string.target_google_maps_traffic_setting_min_traffic_level_mid,
            Color.parseColor("#F23C32"),
            Color.parseColor("#D32F2F")
        ),
        HEAVY_TRAFFIC(
            R.string.target_google_maps_traffic_level_heavy,
            R.string.target_google_maps_traffic_setting_min_traffic_level_heavy,
            Color.parseColor("#811F1F"),
            Color.parseColor("#930000")
        )
    }

    enum class ZoomMode(@StringRes val description: Int, val minValue: Float) {
        IN(R.string.target_google_maps_traffic_setting_distance_zoomed_in, 0.1f),
        OUT(R.string.target_google_maps_traffic_setting_distance_zoomed_out, 0.01f)
    }

}

class GoogleMapsRepositoryImpl(
    private val context: Context,
    private val gson: Gson,
    private val scope: CoroutineScope = MainScope(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
): GoogleMapsRepository {

    private val stateFile = File(context.filesDir, "state.json").apply {
        parentFile?.mkdirs()
    }

    override fun clearTrafficImage() {
        scope.launch {
            writeState(null)
            notifyTarget()
        }
    }

    override fun getTrafficState(): TrafficState? {
        return readState()
    }

    override fun updateTrafficState(hasPermission: Boolean, zoomedIn: Bitmap?, zoomedOut: Bitmap?) {
        scope.launch {
            val state = TrafficState(
                hasPermission,
                zoomedIn,
                zoomedOut,
                zoomedIn?.let { calculateTrafficLevel(it, ZoomMode.IN) },
                zoomedOut?.let { calculateTrafficLevel(it, ZoomMode.OUT) },
            )
            writeState(state)
            notifyTarget()
        }
    }

    private fun notifyTarget() {
        SmartspacerTargetProvider.notifyChange(context, GoogleMapsTrafficTarget::class.java)
    }

    @VisibleForTesting
    suspend fun calculateTrafficLevel(
        image: Bitmap,
        zoomMode: ZoomMode
    ): TrafficLevel = withContext(dispatcher) {
        var noTrafficCount = 0
        var lightTrafficCount = 0
        var midTrafficCount = 0
        var heavyTrafficCount = 0
        for(x in 0 until image.width){
            for(y in 0 until image.height) {
                val pixel = image.getPixel(x, y)
                when {
                    TrafficLevel.NO_TRAFFIC.color.contains(pixel) -> noTrafficCount++
                    TrafficLevel.LIGHT_TRAFFIC.color.contains(pixel) -> lightTrafficCount++
                    TrafficLevel.MID_TRAFFIC.color.contains(pixel) -> midTrafficCount++
                    TrafficLevel.HEAVY_TRAFFIC.color.contains(pixel) -> heavyTrafficCount++
                }
            }
        }
        val total = noTrafficCount + lightTrafficCount + midTrafficCount + heavyTrafficCount
        val highest = arrayOf(
            Pair(TrafficLevel.NO_TRAFFIC, noTrafficCount),
            Pair(TrafficLevel.LIGHT_TRAFFIC, lightTrafficCount),
            Pair(TrafficLevel.MID_TRAFFIC, midTrafficCount),
            Pair(TrafficLevel.HEAVY_TRAFFIC, heavyTrafficCount)
        ).map {
            Pair(it.first, it.second / total.toFloat())
        }.reversed().firstOrNull {
            it.second > zoomMode.minValue
        }?.first ?: TrafficLevel.NO_TRAFFIC
        highest
    }

    private suspend fun writeState(state: TrafficState?) = withContext(Dispatchers.IO) {
        if(state != null) {
            stateFile.writeText(gson.toJson(state))
        }else{
            stateFile.delete()
        }
    }

    private fun readState(): TrafficState? {
        return try {
            if(!stateFile.exists()) return null
            gson.fromJson(stateFile.readText(), TrafficState::class.java)
        }catch (e: Exception) {
            null
        }
    }

}