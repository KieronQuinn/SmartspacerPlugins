package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import com.kieronquinn.app.smartspacer.plugin.amazon.PACKAGE_NAME_GLOBAL
import com.kieronquinn.app.smartspacer.plugin.amazon.PACKAGE_NAME_INDIA
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.AmazonDomain
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getPackageInfoCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isPackageInstalled

fun Context.hasNotificationPermission(): Boolean {
    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}

fun Context.hasDisabledBatteryOptimisation(): Boolean {
    val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(packageName)
}

fun Context.getAmazonMarketplaceDomain(): AmazonDomain? {
    val packageManager = packageManager
    return when {
        packageManager.isPackageInstalled(PACKAGE_NAME_INDIA) -> {
            contentResolver.getMarketplaceDomain(PACKAGE_NAME_INDIA)
        }
        packageManager.isPackageInstalled(PACKAGE_NAME_GLOBAL) -> {
            contentResolver.getMarketplaceDomain(PACKAGE_NAME_GLOBAL)
        }
        else -> null
    }
}

fun Context.getAmazonAppVersion(): String? {
    val packageManager = packageManager
    return when {
        packageManager.isPackageInstalled(PACKAGE_NAME_INDIA) -> {
            packageManager.getPackageInfoCompat(PACKAGE_NAME_INDIA).versionName
        }
        packageManager.isPackageInstalled(PACKAGE_NAME_GLOBAL) -> {
            packageManager.getPackageInfoCompat(PACKAGE_NAME_GLOBAL).versionName
        }
        else -> null
    }
}

private fun ContentResolver.getMarketplaceDomain(packageName: String): AmazonDomain? {
    val uri = Uri.parse("content://$packageName.marketplace.provider")
    val client = acquireUnstableContentProviderClient(uri) ?: return null
    val domainName = try {
        val cursor = client.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        cursor?.getString(0).also {
            cursor?.close()
        }
    }catch (e: Exception){
        null
    } finally {
        client.close()
    }
    return AmazonDomain.entries.firstOrNull { "www.${it.domainName}" == domainName }
}