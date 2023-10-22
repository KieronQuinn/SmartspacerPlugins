package com.kieronquinn.app.smartspacer.plugins.sunrisesunset.utils.extensions

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@SuppressLint("MissingPermission")
suspend fun Context.getLastLocation(): Location? {
    if(!hasPermissions(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION)) return null
    val client = LocationServices.getFusedLocationProviderClient(this)
    return suspendCoroutine { resume ->
        client.lastLocation.addOnCompleteListener {
            resume.resume(it.result)
        }.addOnFailureListener {
            resume.resume(null)
        }
    }
}