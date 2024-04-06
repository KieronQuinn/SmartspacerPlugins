package com.kieronquinn.app.smartspacer.plugin.controls.service

import android.annotation.SuppressLint
import android.app.IActivityManager
import android.app.IActivityTaskManager
import android.app.IApplicationThread
import android.app.IServiceConnection
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Binder
import android.os.IBinder
import android.os.Process
import android.view.KeyEvent
import com.kieronquinn.app.smartspacer.plugin.controls.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.bindServiceInstanceCompat
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.getIdentifier
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.getIntent
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.prepareToLeaveProcess
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.resolveService
import com.topjohnwu.superuser.internal.Utils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import rikka.shizuku.SystemServiceHelper
import kotlin.system.exitProcess

@SuppressLint("RestrictedApi")
class ControlsSuiService: IControlsSuiService.Stub() {

    companion object {
        private const val PERMISSION_BIND_CONTROLS = "android.permission.BIND_CONTROLS"
    }

    private val context = Utils.getContext()
    private val connections = HashMap<Int, Pair<ComponentName, IServiceConnection>>()
    private val connectionsLock = Object()
    private val scope = MainScope()

    private val activityManager by lazy {
        val proxy = SystemServiceHelper.getSystemService("activity")
        IActivityManager.Stub.asInterface(proxy)
    }

    private val activityTaskManager by lazy {
        val proxy = SystemServiceHelper.getSystemService("activity_task")
        IActivityTaskManager.Stub.asInterface(proxy)
    }

    override fun ping(): Boolean {
        return true
    }

    override fun isCompatible(): Boolean {
        return runWithClearedIdentity {
            context.checkCallingOrSelfPermission(PERMISSION_BIND_CONTROLS) == PERMISSION_GRANTED
        }
    }

    override fun bindServicePriviliged(
        applicationThread: IBinder?,
        activityToken: IBinder?,
        serviceConnection: IBinder,
        intent: Intent,
        flags: Int
    ): Int {
        val caller = applicationThread?.let {
            IApplicationThread.Stub.asInterface(it)
        }
        val componentName = intent.resolveService(context)
            ?: return -1
        val connection = Pair(componentName, IServiceConnection.Stub.asInterface(serviceConnection))
        val identifier = Process.myUserHandle().getIdentifier()
        val connectionHashCode = connection.hashCode()
        connections[connectionHashCode] = connection
        intent.prepareToLeaveProcess(context)
        runWithClearedIdentity {
            activityManager.bindServiceInstanceCompat(
                caller,
                activityToken,
                intent,
                null,
                connection.second,
                flags,
                null,
                BuildConfig.APPLICATION_ID,
                identifier
            )
        }
        return connectionHashCode
    }

    override fun unbindService(token: Int) {
        synchronized(connectionsLock) {
            val connection = connections[token]?.second ?: return
            unbindService(connection)
            connections.remove(token)
        }
    }

    override fun startActivity(intent: Intent) {
        runWithClearedIdentity {
            activityTaskManager.startActivity(
                null,
                "android",
                null,
                intent,
                intent.resolveType(context),
                null,
                null,
                0,
                0,
                null,
                null
            )
        }
    }

    override fun startPendingIntent(pendingIntent: PendingIntent) {
        runWithClearedIdentity {
            val intent = pendingIntent.getIntent()
            startActivity(intent)
        }
    }

    private fun unbindService(connection: IServiceConnection) {
        val result = activityManager.unbindService(connection)
    }

    override fun destroy() {
        //Clean up any remaining connected services
        runBlocking {
            synchronized(connectionsLock) {
                connections.values.forEach { unbindService(it.second) }
                connections.clear()
            }
        }
        scope.cancel()
        exitProcess(0)
    }

    override fun forceStop(packageName: String, userId: Int) {
        runWithClearedIdentity {
            activityManager.forceStopPackage(packageName, userId)
        }
    }

    override fun stopService(applicationThread: IBinder?, intent: Intent): Int {
        return runWithClearedIdentity {
            val caller = applicationThread?.let { IApplicationThread.Stub.asInterface(it) }
            activityManager.stopService(
                caller,
                intent,
                intent.resolveType(context),
                Process.myUserHandle().getIdentifier()
            )
        }
    }

    override fun showPowerMenu() {
        runCommand("input keyevent ${KeyEvent.KEYCODE_POWER}")
    }

    private fun runCommand(vararg commands: String) {
        Runtime.getRuntime().let { runtime ->
            commands.forEach { runtime.exec(it) }
        }
    }

    @Synchronized
    private fun <T> runWithClearedIdentity(block: () -> T): T {
        val token = Binder.clearCallingIdentity()
        return block().also {
            Binder.restoreCallingIdentity(token)
        }
    }

}