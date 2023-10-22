package com.kieronquinn.app.shared.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 *  Generates a Bitmap of a Google Map of a given [width]x[height], and with the given [markers].
 *  The camera will automatically be set to show all the markers, at n-1 zoom level for sufficient
 *  padding.
 */
suspend fun Context.generateGoogleMap(
    width: Int,
    height: Int,
    padding: Rect,
    zoomLevel: Float? = null,
    markers: GoogleMap.() -> List<MarkerOptions>
): Bitmap? = withTimeout(5000L) {
    withContext(Dispatchers.Main) {
        val options = GoogleMapOptions().liteMode(true)
        val mapView = MapView(this@generateGoogleMap, options)
        mapView.onCreate(null)
        mapView.onResume()
        mapView.getMap().run {
            setPadding(padding.left, padding.top, padding.right, padding.bottom)
            mapView.layoutParams = ViewGroup.LayoutParams(width, height)
            mapView.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            )
            mapView.layout(0, 0, width, height)
            val addedMarkers = markers(this)
            val bounds = LatLngBounds.builder()
            addedMarkers.forEach {
                addMarker(it)
                bounds.include(it.position)
            }
            moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 0))
            if(zoomLevel != null){
                moveCamera(CameraUpdateFactory.zoomTo(zoomLevel))
            }else {
                moveCamera(CameraUpdateFactory.zoomOut())
            }
            awaitReady()
            generateSnapshot()
        }
    }
}

fun Context.generateMarker(location: LatLng, @DrawableRes iconRes: Int): MarkerOptions {
    return MarkerOptions()
        .position(location)
        .icon(vectorToBitmap(iconRes))
}

private fun Context.vectorToBitmap(@DrawableRes id: Int): BitmapDescriptor {
    val vectorDrawable = ResourcesCompat.getDrawable(resources, id, null)
    val bitmap = Bitmap.createBitmap(
        vectorDrawable!!.intrinsicWidth,
        vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
    vectorDrawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

private suspend fun MapView.getMap() = suspendCoroutine {
    getMapAsync { map ->
        it.resume(map)
    }
}

private suspend fun GoogleMap.awaitReady() = suspendCoroutine {
    setOnMapLoadedCallback {
        it.resume(Unit)
    }
}

private suspend fun GoogleMap.generateSnapshot() = suspendCoroutine {
    snapshot { bitmap ->
        it.resume(bitmap)
    }
}