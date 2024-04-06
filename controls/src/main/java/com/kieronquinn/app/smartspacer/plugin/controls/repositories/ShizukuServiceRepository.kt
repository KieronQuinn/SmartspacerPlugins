package com.kieronquinn.app.smartspacer.plugin.controls.repositories

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.RemoteException
import com.kieronquinn.app.smartspacer.plugin.controls.BuildConfig
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ShizukuServiceRepository.ShizukuServiceResponse
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ShizukuServiceRepository.ShizukuServiceResponse.FailureReason
import com.kieronquinn.app.smartspacer.plugin.controls.service.ControlsSuiService
import com.kieronquinn.app.smartspacer.plugin.controls.service.IControlsSuiService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import org.koin.core.component.KoinComponent
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

interface ShizukuServiceRepository {

    sealed class ShizukuServiceResponse<T> {
        data class Success<T>(val result: T): ShizukuServiceResponse<T>()
        data class Failed<T>(val reason: FailureReason): ShizukuServiceResponse<T>()

        enum class FailureReason {
            /**
             *  Shizuku is not bound, likely the user has not started it since rebooting
             */
            NO_BINDER,

            /**
             *  Permission to access Shizuku has not been granted
             */
            PERMISSION_DENIED,

            /**
             *  The service is not immediately available (only used in [runWithServiceIfAvailable])
             */
            NOT_AVAILABLE
        }

        /**
         *  Unwraps a result into either its value or null if it failed
         */
        fun unwrap(): T? {
            return (this as? Success)?.result
        }
    }

    val isReady: StateFlow<Boolean>
    val suiService: StateFlow<IControlsSuiService?>

    suspend fun assertReady(): Boolean
    suspend fun <T> runWithService(block: suspend (IControlsSuiService) -> T): ShizukuServiceResponse<T>
    fun <T> runWithServiceIfAvailable(block: (IControlsSuiService) -> T): ShizukuServiceResponse<T>
    fun disconnect()

}

class ShizukuServiceRepositoryImpl(
    context: Context
): ShizukuServiceRepository, KoinComponent {

    companion object {
        private const val SHIZUKU_PERMISSION_REQUEST_CODE = 1001
        private const val SHIZUKU_TIMEOUT = 60_000L
    }

    private val suiComponent by lazy {
        ComponentName(context, ControlsSuiService::class.java)
    }

    private val suiUserServiceArgs by lazy {
        Shizuku.UserServiceArgs(suiComponent).apply {
            daemon(false)
            debuggable(BuildConfig.DEBUG)
            version(BuildConfig.VERSION_CODE)
            processNameSuffix("sui")
        }
    }

    private var serviceConnection: ServiceConnection? = null
    private val shizukuServiceLock = Mutex()
    private val shizukuRunLock = Mutex()
    private val scope = MainScope()

    override val suiService = MutableStateFlow<IControlsSuiService?>(null)

    private val binderReceived = callbackFlow {
        val listener = Shizuku.OnBinderReceivedListener {
            trySend(System.currentTimeMillis())
        }
        Shizuku.addBinderReceivedListener(listener)
        awaitClose {
            Shizuku.removeBinderReceivedListener(listener)
        }
    }.stateIn(scope, SharingStarted.Eagerly, System.currentTimeMillis())

    private val binderDestroyed = callbackFlow {
        val listener = Shizuku.OnBinderDeadListener {
            //Clear service cache to sever the connection as we can't call unbind
            scope.launch {
                serviceConnection = null
                suiService.emit(null)
                trySend(System.currentTimeMillis())
            }
        }
        Shizuku.addBinderDeadListener(listener)
        awaitClose {
            Shizuku.removeBinderDeadListener(listener)
        }
    }.stateIn(scope, SharingStarted.Eagerly, System.currentTimeMillis())

    private val binderReady = combine(binderReceived, binderDestroyed) { _, _ ->
        Shizuku.pingBinder()
    }.stateIn(scope, SharingStarted.Eagerly, Shizuku.pingBinder())

    override val isReady = binderReady.map {
        assertReady()
    }.stateIn(scope, SharingStarted.Eagerly, false)

    override suspend fun assertReady(): Boolean {
        val rawResult = runWithService {
            it.ping()
        }
        val result = rawResult.unwrap()
        return result == true
    }

    override suspend fun <T> runWithService(
        block: suspend (IControlsSuiService) -> T
    ): ShizukuServiceResponse<T> = shizukuRunLock.withLock {
        return runWithServiceLocked(block)
    }

    private suspend fun <T> runWithServiceLocked(
        block: suspend (IControlsSuiService) -> T
    ): ShizukuServiceResponse<T> = withTimeout(SHIZUKU_TIMEOUT) {
        suiService.value?.let {
            if(!it.safePing()){
                //Service has disconnected or died
                suiService.emit(null)
                serviceConnection = null
                return@let
            }
            val result = try {
                block(it)
            }catch (e: RuntimeException){
                return@withTimeout ShizukuServiceResponse.Failed(FailureReason.NOT_AVAILABLE)
            }
            return@withTimeout ShizukuServiceResponse.Success(result)
        }
        if(!Shizuku.pingBinder())
            return@withTimeout ShizukuServiceResponse.Failed(FailureReason.NO_BINDER)
        if(!requestPermission())
            return@withTimeout ShizukuServiceResponse.Failed(FailureReason.PERMISSION_DENIED)
        val result = try {
            block(getService()
                ?: return@withTimeout ShizukuServiceResponse.Failed(FailureReason.NOT_AVAILABLE))
        }catch (e: RuntimeException){
            return@withTimeout ShizukuServiceResponse.Failed(FailureReason.NOT_AVAILABLE)
        }
        return@withTimeout ShizukuServiceResponse.Success(result)
    }

    override fun <T> runWithServiceIfAvailable(
        block: (IControlsSuiService) -> T
    ): ShizukuServiceResponse<T> {
        return try {
            suiService.value?.let {
                ShizukuServiceResponse.Success(block(it))
            } ?: ShizukuServiceResponse.Failed(FailureReason.NOT_AVAILABLE)
        }catch (e: RuntimeException){
            ShizukuServiceResponse.Failed(FailureReason.NOT_AVAILABLE)
        }
    }

    override fun disconnect() {
        serviceConnection?.let {
            Shizuku.unbindUserService(suiUserServiceArgs, it, true)
        }
    }

    private suspend fun requestPermission() = suspendCancellableCoroutine {
        var hasResumed = false
        if(Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            if(!hasResumed) {
                hasResumed = true
                it.resume(true) //Already granted
            }
            return@suspendCancellableCoroutine
        }
        val listener = object: Shizuku.OnRequestPermissionResultListener {
            override fun onRequestPermissionResult(requestCode: Int, grantResult: Int) {
                if(requestCode != SHIZUKU_PERMISSION_REQUEST_CODE) return
                Shizuku.removeRequestPermissionResultListener(this)
                if(!hasResumed) {
                    hasResumed = true
                    it.resume(grantResult == PackageManager.PERMISSION_GRANTED)
                }
            }
        }
        Shizuku.addRequestPermissionResultListener(listener)
        Shizuku.requestPermission(SHIZUKU_PERMISSION_REQUEST_CODE)
        it.invokeOnCancellation {
            Shizuku.removeRequestPermissionResultListener(listener)
        }
    }

    private suspend fun getService() = shizukuServiceLock.withLock {
        suspendCoroutine {
            var hasResumed = false
            val serviceConnection = object: ServiceConnection {
                override fun onServiceConnected(component: ComponentName, binder: IBinder) {
                    serviceConnection = this
                    val service = IControlsSuiService.Stub.asInterface(binder)
                    scope.launch {
                        this@ShizukuServiceRepositoryImpl.suiService.emit(service)
                        if(!hasResumed){
                            hasResumed = true
                            it.resume(service)
                        }
                    }
                }

                override fun onServiceDisconnected(component: ComponentName) {
                    serviceConnection = null
                    scope.launch {
                        suiService.emit(null)
                    }
                }
            }
            try {
                Shizuku.bindUserService(suiUserServiceArgs, serviceConnection)
            }catch (e: RuntimeException) {
                //Shizuku died
                it.resume(null)
            }
        }
    }

    private fun IControlsSuiService.safePing(): Boolean {
        return try {
            ping()
        }catch (e: RemoteException){
            false
        }
    }

}