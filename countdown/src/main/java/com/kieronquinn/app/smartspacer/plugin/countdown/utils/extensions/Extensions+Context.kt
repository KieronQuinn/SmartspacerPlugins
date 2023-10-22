package com.kieronquinn.app.smartspacer.plugin.countdown.utils.extensions

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn

fun Context.hasAlarmPermission(scope: CoroutineScope): StateFlow<Boolean> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        hasAlarmPermissionApi31(scope)
    } else {
        //Always granted on < S
        MutableStateFlow(true)
    }
}

@RequiresApi(Build.VERSION_CODES.S)
private fun Context.hasAlarmPermissionApi31(scope: CoroutineScope) = callbackFlow<Boolean> {
    val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            trySend(hasAlarmPermission())
        }
    }
    registerReceiver(
        receiver,
        IntentFilter(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)
    )
    awaitClose {
        unregisterReceiver(receiver)
    }
}.stateIn(scope, SharingStarted.Eagerly, hasAlarmPermission())

@RequiresApi(Build.VERSION_CODES.S)
private fun Context.hasAlarmPermission(): Boolean {
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    return alarmManager.canScheduleExactAlarms()
}