package com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions

import android.app.IApplicationThread
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.service.controls.IControlsProvider
import com.kieronquinn.app.smartspacer.plugin.controls.service.IControlsSuiService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun IControlsSuiService.bindService(
    context: Context,
    intent: Intent,
    serviceConnection: ServiceConnection,
    flags: Int = 0,
    applicationThread: IApplicationThread? = context.getIApplicationThread(),
    activityToken: IBinder? = context.getActivityToken()
): Int {
    val mainThreadHandler = context.getMainThreadHandler()
    val serviceDispatcher =
        context.getServiceDispatcher(serviceConnection, mainThreadHandler, flags)
    return bindServicePriviliged(
        applicationThread?.asBinder(),
        activityToken,
        serviceDispatcher.asBinder(),
        intent,
        flags
    )
}

fun IControlsSuiService.getControlsProvider(
    context: Context,
    intent: Intent
): Flow<IControlsProvider?> = callbackFlow {
    var token: Int? = null
    var isDisconnecting = false
    val bindService = { serviceConnection: ServiceConnection ->
        bindService(
            context,
            intent,
            serviceConnection,
            activityToken = null,
            flags = Context.BIND_AUTO_CREATE
        )
    }
    val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val controlsProvider = IControlsProvider.Stub.asInterface(service)
            trySend(controlsProvider)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trySend(null)
            token = null
            //If we've not specifically disconnected via awaitClose, reconnect
            if(!isDisconnecting) {
                token = bindService(this)
            }
        }
    }
    if(isCompatible) {
        token = bindService(serviceConnection)
    }else{
        trySend(null)
    }
    awaitClose {
        isDisconnecting = true
        token?.let { unbindService(it) }
    }
}