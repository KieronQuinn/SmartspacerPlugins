package com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions

import android.annotation.SuppressLint
import android.app.IActivityManager
import android.app.IApplicationThread
import android.app.IServiceConnection
import android.content.Intent
import android.os.Build
import android.os.IBinder

@SuppressLint("UnsafeOptInUsageError")
fun IActivityManager.bindServiceInstanceCompat(
    caller: IApplicationThread?,
    token: IBinder?,
    service: Intent?,
    resolvedType: String?,
    connection: IServiceConnection?,
    flags: Int,
    instanceName: String?,
    callingPackage: String?,
    userId: Int
): Int {
    return when {
        Build.VERSION.SDK_INT >= 34 -> {
            bindServiceInstance(
                caller,
                token,
                service,
                resolvedType,
                connection,
                flags.toLong(),
                instanceName,
                callingPackage,
                userId
            )
        }
        Build.VERSION.SDK_INT >= 33 -> {
            bindServiceInstance(
                caller,
                token,
                service,
                resolvedType,
                connection,
                flags,
                instanceName,
                callingPackage,
                userId
            )
        }
        else -> {
            bindIsolatedService(
                caller,
                token,
                service,
                resolvedType,
                connection,
                flags,
                instanceName,
                callingPackage,
                userId
            )
        }
    }
}