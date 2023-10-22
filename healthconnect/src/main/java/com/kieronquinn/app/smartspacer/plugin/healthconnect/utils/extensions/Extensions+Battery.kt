package com.kieronquinn.app.smartspacer.plugin.healthconnect.utils.extensions

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import com.kieronquinn.app.smartspacer.plugin.healthconnect.BuildConfig

fun Context.hasDisabledBatteryOptimisation(): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.hasDisabledBatteryOptimisation()
}

fun PowerManager.hasDisabledBatteryOptimisation(): Boolean {
    return isIgnoringBatteryOptimizations(BuildConfig.APPLICATION_ID)
}

@SuppressLint("BatteryLife")
fun getBatteryOptimisationIntent(): Intent {
    return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
    }
}