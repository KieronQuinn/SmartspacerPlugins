package com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.kieronquinn.app.smartspacer.plugin.amazon.AmazonPluginApplication.Companion.PACKAGE_NAME_GLOBAL
import com.kieronquinn.app.smartspacer.plugin.amazon.AmazonPluginApplication.Companion.PACKAGE_NAME_INDIA
import com.kieronquinn.app.smartspacer.plugin.amazon.model.AmazonDomain
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.getPackageInfoCompat
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isPackageInstalled

fun Context.getAmazonMarketplaceDomain(): AmazonDomain {
    val packageManager = packageManager
    return when {
        packageManager.isPackageInstalled(PACKAGE_NAME_INDIA) -> {
            contentResolver.getMarketplaceDomain(PACKAGE_NAME_INDIA)
        }
        packageManager.isPackageInstalled(PACKAGE_NAME_GLOBAL) -> {
            contentResolver.getMarketplaceDomain(PACKAGE_NAME_GLOBAL)
        }
        else -> AmazonDomain.UNKNOWN
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

private fun ContentResolver.getMarketplaceDomain(packageName: String): AmazonDomain {
    val uri = Uri.parse("content://$packageName.marketplace.provider")
    val client = acquireUnstableContentProviderClient(uri) ?: return AmazonDomain.UNKNOWN
    val domainName = try {
        val cursor = client.query(uri, null, null, null, null)
        cursor?.moveToFirst()
        cursor?.getString(0).also {
            cursor?.close()
        }
    }catch (e: Exception){
        AmazonDomain.UNKNOWN.domainName
    } finally {
        client.close()
    }
    return AmazonDomain.values().firstOrNull { it.domainName == domainName } ?: AmazonDomain.UNKNOWN
}