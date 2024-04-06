package com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions

import android.content.ComponentName
import android.os.DeadObjectException
import android.os.IBinder
import android.service.controls.Control
import android.service.controls.IControlsActionCallback
import android.service.controls.IControlsProvider
import android.service.controls.IControlsSubscriber
import android.service.controls.IControlsSubscription
import android.service.controls.actions.ControlAction
import android.service.controls.actions.ControlActionWrapper
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

fun IControlsProvider.getAllControls(): Flow<List<Control>> = callbackFlow {
    val controls = ArrayList<Control>()
    var subscription: IControlsSubscription? = null
    val subscriber = object: IControlsSubscriber.Stub() {
        override fun onSubscribe(token: IBinder, cs: IControlsSubscription) {
            subscription = cs
            cs.request(Long.MAX_VALUE)
        }

        override fun onNext(token: IBinder, c: Control) {
            controls.add(c)
        }

        override fun onError(token: IBinder, s: String) {
            trySend(controls)
        }

        override fun onComplete(token: IBinder) {
            trySend(controls)
        }
    }
    load(subscriber)
    awaitClose {
        try {
            subscription?.cancel()
        }catch (e: DeadObjectException){
            //Disconnected
        }
    }
}

fun IControlsProvider.action(
    controlId: String,
    action: ControlAction
) = callbackFlow {
    val wrapper = ControlActionWrapper(action)
    val actionCallback = object: IControlsActionCallback.Stub() {
        override fun accept(token: IBinder, controlId: String, response: Int) {
            trySend(response)
        }
    }
    action(controlId, wrapper, actionCallback)
    awaitClose {
        //No-op
    }
}

fun IControlsProvider.subscribe(
    componentName: ComponentName,
    vararg controlConfigs: ControlConfig
): Flow<ControlsState> = callbackFlow {
    val controls: MutableMap<ControlConfig, Control?> =
        controlConfigs.associateWith { null }.toMutableMap()
    var subscription: IControlsSubscription? = null
    val subscriber = object: IControlsSubscriber.Stub() {
        override fun onSubscribe(token: IBinder, cs: IControlsSubscription) {
            subscription = cs
            cs.request(Long.MAX_VALUE)
        }

        override fun onComplete(token: IBinder) {
        }

        override fun onError(token: IBinder, s: String) {
            trySend(ControlsState.Error(componentName, s, controlConfigs.toList()))
        }

        override fun onNext(token: IBinder, c: Control) {
            controls.forEach {
                if(it.key.controlId == c.controlId) {
                    controls[it.key] = c
                }
            }
            trySend(ControlsState.Controls(
                componentName, controlConfigs.toList(), controls.filterNotNull()
            ))
        }
    }
    val controlIds = controlConfigs.map { it.controlId }.distinct().toTypedArray()
    subscribe(controlIds.toList(), subscriber)
    awaitClose {
        try {
            subscription?.cancel()
        }catch (e: DeadObjectException){
            //Disconnected
        }
    }
}

private fun Map<ControlConfig, Control?>.filterNotNull(): Map<ControlConfig, Control> {
    return filterNot { it.value == null } as Map<ControlConfig, Control>
}

sealed class ControlsState(
    open val componentName: ComponentName,
    open val controlConfigs: List<ControlConfig>
) {
    data class Loading(
        override val componentName: ComponentName,
        override val controlConfigs: List<ControlConfig>
    ): ControlsState(componentName, controlConfigs)
    data class Controls(
        override val componentName: ComponentName,
        override val controlConfigs: List<ControlConfig>,
        val controls: Map<ControlConfig, Control>
    ): ControlsState(componentName, controlConfigs)
    data class Error(
        override val componentName: ComponentName,
        val error: String,
        override val controlConfigs: List<ControlConfig>
    ): ControlsState(componentName, controlConfigs)
}