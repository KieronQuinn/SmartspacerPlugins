package com.kieronquinn.app.smartspacer.plugins.datausage.utils.extensions

import android.app.AppOpsManager
import android.app.AppOpsManager.MODE_ALLOWED
import android.app.AppOpsManager.OPSTR_GET_USAGE_STATS
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Process

fun Context.hasDataUsagePermission(): Boolean {
    val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    return appOpsManager.unsafeCheckOpNoThrow(
        OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        packageName
    ) == MODE_ALLOWED
}

@Suppress("DEPRECATION")
fun Context.hasEthernet(): Boolean {
    if(!packageManager.hasSystemFeature(PackageManager.FEATURE_ETHERNET) &&
        !packageManager.hasSystemFeature(PackageManager.FEATURE_USB_HOST)) return false
    return networkHasSomeUsage(ConnectivityManager.TYPE_ETHERNET)
}

@Suppress("DEPRECATION")
fun Context.hasWiFi(): Boolean {
    if(!packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI)) return false
    return networkHasSomeUsage(ConnectivityManager.TYPE_WIFI)
}

@Suppress("DEPRECATION")
fun Context.hasMobileData(): Boolean {
    if(!packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY_RADIO_ACCESS)) return false
    return networkHasSomeUsage(ConnectivityManager.TYPE_WIFI)
}

private fun Context.networkHasSomeUsage(type: Int): Boolean {
    val networkStatsManager =getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
    return networkStatsManager.querySummaryForDevice(
        type,
        null,
        0L,
        System.currentTimeMillis()
    )?.let {
        it.rxBytes > 0 || it.txBytes > 0
    } ?: false
}