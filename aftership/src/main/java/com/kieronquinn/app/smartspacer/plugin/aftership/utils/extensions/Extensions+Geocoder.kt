package com.kieronquinn.app.smartspacer.plugin.aftership.utils.extensions

import android.location.Address
import android.location.Geocoder
import android.location.Geocoder.GeocodeListener
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.android.gms.maps.model.LatLng
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun Geocoder.getFromLocationName(locationName: String): LatLng? {
    return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getFromLocationName33(locationName, 1)
    }else{
        getFromLocationName32(locationName, 1)
    }.firstOrNull()?.let {
        LatLng(it.latitude, it.longitude)
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
private suspend fun Geocoder.getFromLocationName33(
    locationName: String,
    maxResults: Int
): List<Address> = suspendCoroutine {
    getFromLocationName(locationName, maxResults, object: GeocodeListener {
        override fun onGeocode(addresses: MutableList<Address>) {
            it.resume(addresses)
        }

        override fun onError(errorMessage: String?) {
            it.resume(emptyList())
        }
    })
}

@Suppress("DEPRECATION")
private fun Geocoder.getFromLocationName32(
    locationName: String,
    maxResults: Int
): List<Address> {
    return getFromLocationName(locationName, maxResults) ?: emptyList()
}