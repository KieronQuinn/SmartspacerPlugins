package com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.IApplicationThread
import android.app.IServiceConnection
import android.app.KeyguardManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.ContextHidden
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.PowerManager
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.resolveActivityCompat
import dev.rikka.tools.refine.Refine
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("UnsafeOptInUsageError")
fun Context.getServiceDispatcher(
    serviceConnection: ServiceConnection,
    handler: Handler,
    flags: Int
): IServiceConnection {
    return if(Build.VERSION.SDK_INT >= 34){
        Refine.unsafeCast<ContextHidden>(this)
            .getServiceDispatcher(serviceConnection, handler, flags.toLong())
    }else{
        Refine.unsafeCast<ContextHidden>(this)
            .getServiceDispatcher(serviceConnection, handler, flags)
    }
}

fun Context.getMainThreadHandler(): Handler {
    return Refine.unsafeCast<ContextHidden>(this).mainThreadHandler
}

fun Context.getIApplicationThread(): IApplicationThread {
    return Refine.unsafeCast<ContextHidden>(this).iApplicationThread
}

fun Context.getActivityToken(): IBinder? {
    return Refine.unsafeCast<ContextHidden>(this).activityToken
}

fun Context.hasNotificationPermission(): Boolean {
    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
    return checkCallingOrSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Context.registerReceiverCompat(receiver: BroadcastReceiver, intentFilter: IntentFilter) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        registerReceiver(receiver, intentFilter, Context.RECEIVER_EXPORTED)
    }else{
        registerReceiver(receiver, intentFilter)
    }
}

fun <T> Context.broadcastReceiverAsFlow(
    vararg actions: String,
    map: (Intent) -> T,
    startWith: (() -> T)? = null
) = callbackFlow {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(map(intent))
        }
    }
    actions.forEach {
        registerReceiverCompat(receiver, IntentFilter(it))
    }
    startWith?.invoke()?.let {
        trySend(it)
    }
    awaitClose {
        unregisterReceiver(receiver)
    }
}


fun Context.isLocked(): Boolean {
    getSystemService(Context.POWER_SERVICE) as PowerManager
    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return keyguardManager.isKeyguardLocked
}

fun Context.locked(): Flow<Boolean> {
    return broadcastReceiverAsFlow(
        Intent.ACTION_SCREEN_OFF, Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT,
        map = {
            isLocked()
        },
        startWith = {
            isLocked()
        }
    )
}

//Safe to use getRunningServices for our own service
@Suppress("deprecation")
fun Context.isServiceRunning(serviceClass: Class<out Service>): Boolean {
    val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    return activityManager.getRunningServices(Integer.MAX_VALUE).any {
        it?.service?.className == serviceClass.name
    }
}

private const val PACKAGE_KEYGUARD = "com.android.systemui"

private val CONTROLS_ACTIVITIES = arrayOf(
    "$PACKAGE_KEYGUARD.controls.ui.ControlsActivity",
    "$PACKAGE_KEYGUARD.controls.SamsungControlsMainActivity"
)

fun Context.getControlsIntent(): Intent? {
    return CONTROLS_ACTIVITIES.firstNotNullOfOrNull {
        val intent = Intent().apply {
            component = ComponentName(PACKAGE_KEYGUARD, it)
        }
        intent.takeIf { packageManager.resolveActivityCompat(intent) != null }
    }
}