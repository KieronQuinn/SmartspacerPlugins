package com.kieronquinn.app.smartspacer.plugin.controls.repositories

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.os.Parcelable
import android.os.Process
import android.service.controls.Control
import android.service.controls.IControlsProvider
import android.service.controls.actions.BooleanAction
import android.service.controls.actions.ControlAction
import android.service.controls.actions.FloatAction
import android.service.controls.actions.ModeAction
import com.kieronquinn.app.smartspacer.plugin.controls.complications.ControlsComplication
import com.kieronquinn.app.smartspacer.plugin.controls.complications.ControlsComplication.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.controls.components.controls.templates.ControlsTemplate
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlExtraData
import com.kieronquinn.app.smartspacer.plugin.controls.model.ControlTapAction
import com.kieronquinn.app.smartspacer.plugin.controls.model.LoadingConfig
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlConfig
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlState
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ControlsRepository.ControlsApp
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ShizukuServiceRepository.ShizukuServiceResponse
import com.kieronquinn.app.smartspacer.plugin.controls.repositories.ShizukuServiceRepository.ShizukuServiceResponse.FailureReason
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement
import com.kieronquinn.app.smartspacer.plugin.controls.requirements.ControlsRequirement.RequirementData
import com.kieronquinn.app.smartspacer.plugin.controls.targets.ControlsTarget
import com.kieronquinn.app.smartspacer.plugin.controls.targets.ControlsTarget.TargetData
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.ACTION_CONTROLS_PROVIDER
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.ControlsState
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.action
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.getAllControls
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.getControlsProvider
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.getIdentifier
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.makeControlsIntent
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.resolveService
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.subscribe
import com.kieronquinn.app.smartspacer.plugin.controls.utils.extensions.timeoutFirst
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.firstNotNull
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.queryIntentServicesCompat
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerRequirementProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.parcelize.Parcelize

interface ControlsRepository {

    fun startListeningFromWake()
    fun startListening()
    fun stopListening()
    fun overrideListening(listen: Boolean)

    fun getControlsApps(): List<ControlsApp>

    suspend fun getControlsForApp(componentName: ComponentName): List<Control>?

    fun getControl(
        componentName: ComponentName,
        controlId: String,
        smartspacerId: String
    ): ControlState?

    fun getControlAsFlow(
        componentName: ComponentName,
        controlId: String,
        smartspacerId: String
    ): Flow<ControlState?>

    fun runControlTapAction(
        action: ControlTapAction,
        controlExtraData: ControlExtraData,
        control: Control,
        componentName: ComponentName,
        smartspacerId: String,
        callback: (Int, ControlTapAction, ControlExtraData) -> Unit
    )

    fun runControlAction(
        control: Control,
        componentName: ComponentName,
        controlAction: ControlAction,
        controlTapAction: ControlTapAction,
        controlExtraData: ControlExtraData,
        smartspacerId: String,
        resultCallback: (Int, ControlTapAction, ControlExtraData) -> Unit,
        callback: (Context, Control, ComponentName, ControlExtraData, Int, String, (Int, ControlTapAction, ControlExtraData) -> Unit) -> Unit
    )

    fun launchPanelIntent(componentName: ComponentName)

    fun launchAppPendingIntent(pendingIntent: PendingIntent)

    fun setControlOverride(componentName: ComponentName, control: Control)

    fun getPanelIntent(componentName: ComponentName): Intent?

    fun launchIntentAsRoot(intent: Intent)

    fun showPowerMenu()

    suspend fun subscribeOnce(
        componentName: ComponentName,
        controlId: String,
        smartspacerId: String
    ): Flow<Control?>

    @Parcelize
    data class ControlsApp(
        val icon: Icon,
        val componentName: ComponentName,
        val name: CharSequence
    ): Parcelable

    data class ControlConfig(
        val componentName: ComponentName,
        val controlId: String,
        val smartspacerId: String,
        val loadingConfig: LoadingConfig
    )

    sealed class ControlState(
        open val componentName: ComponentName,
        open val controlId: String,
        open val smartspacerId: String,
        open val cachedIcon: Icon?,
        open val cachedTitle: CharSequence?,
        open val loadedFromCache: Boolean,
        open val loadedAt: Long
    ) {
        data class Control(
            override val componentName: ComponentName,
            val control: android.service.controls.Control,
            override val smartspacerId: String,
            override val cachedIcon: Icon?,
            override val cachedTitle: CharSequence?,
            override val loadedFromCache: Boolean = false,
            override val loadedAt: Long = System.currentTimeMillis()
        ): ControlState(
            componentName,
            control.controlId,
            smartspacerId,
            cachedIcon,
            cachedTitle,
            loadedFromCache,
            loadedAt
        )
        data class Loading(
            override val componentName: ComponentName,
            override val controlId: String,
            override val smartspacerId: String,
            override val cachedIcon: Icon?,
            override val cachedTitle: CharSequence?,
            override val loadedFromCache: Boolean = false,
            override val loadedAt: Long = System.currentTimeMillis()
        ): ControlState(
            componentName,
            controlId,
            smartspacerId,
            cachedIcon,
            cachedTitle,
            loadedFromCache,
            loadedAt
        )
        data class Sending(
            override val componentName: ComponentName,
            override val controlId: String,
            override val smartspacerId: String,
            override val cachedIcon: Icon?,
            override val cachedTitle: CharSequence?,
            override val loadedFromCache: Boolean = false,
            override val loadedAt: Long = System.currentTimeMillis()
        ): ControlState(
            componentName,
            controlId,
            smartspacerId,
            cachedIcon,
            cachedTitle,
            loadedFromCache,
            loadedAt
        )
        data class Hidden(
            override val componentName: ComponentName,
            override val controlId: String,
            override val smartspacerId: String,
            override val cachedIcon: Icon?,
            override val cachedTitle: CharSequence?,
            override val loadedFromCache: Boolean = false,
            override val loadedAt: Long = System.currentTimeMillis()
        ): ControlState(
            componentName,
            controlId,
            smartspacerId,
            cachedIcon,
            cachedTitle,
            loadedFromCache,
            loadedAt
        )
        data class Error(
            override val componentName: ComponentName,
            override val controlId: String,
            override val smartspacerId: String,
            override val cachedIcon: Icon?,
            override val cachedTitle: CharSequence?,
            override val loadedFromCache: Boolean = false,
            override val loadedAt: Long = System.currentTimeMillis()
        ): ControlState(
            componentName,
            smartspacerId,
            controlId,
            cachedIcon,
            cachedTitle,
            loadedFromCache,
            loadedAt
        )

        fun copyCached(): ControlState {
            return when(this) {
                is Control -> copy(loadedFromCache = true)
                is Loading -> copy(loadedFromCache = true)
                is Sending -> copy(loadedFromCache = true)
                is Hidden -> copy(loadedFromCache = true)
                is Error -> copy(loadedFromCache = true)
            }
        }
    }

}

class ControlsRepositoryImpl(
    private val context: Context,
    private val shizukuServiceRepository: ShizukuServiceRepository,
    private val settingsRepository: ControlsSettingsRepository,
    dataRepository: DataRepository
): ControlsRepository {

    companion object {
        private const val SERVICE_TIMEOUT = 5_000L // 5 seconds
        private const val LISTEN_TIMEOUT = 10_000L // 10 seconds
        private const val META_DATA_PANEL_ACTIVITY =
            "android.service.controls.META_DATA_PANEL_ACTIVITY"
    }

    private val scope = MainScope()
    private val packageManager = context.packageManager
    private val serviceLock = Mutex()
    private val controlsProviders =
        HashMap<ComponentName, Flow<ShizukuServiceResponse<IControlsProvider?>>>()
    private var cachedState: List<ControlsAppState> = emptyList()
    private val reloadBus = MutableStateFlow(System.currentTimeMillis())
    private val shouldListen = MutableStateFlow(false)
    private val listenOverride = MutableStateFlow(false)
    private val controlOverrides = MutableStateFlow<Set<ControlOverride>>(emptySet())
    private val controlOverridesLock = Mutex()
    private var subscribeJob: Job? = null

    private val complicationControls = dataRepository.getAllComplicationData(
        ComplicationData.TYPE, ComplicationData::class.java
    ).map { complications ->
        complications.mapNotNull { item ->
            ControlConfig(
                item.componentName ?: return@mapNotNull null,
                item.controlId ?: return@mapNotNull null,
                item.smartspacerId,
                item.loadConfig
            )
        }
    }

    private val targetControls = dataRepository.getAllTargetData(
        TargetData.TYPE, TargetData::class.java
    ).map { targets ->
        targets.mapNotNull { item ->
            ControlConfig(
                item.componentName ?: return@mapNotNull null,
                item.controlId ?: return@mapNotNull null,
                item.smartspacerId,
                item.loadConfig
            )
        }
    }

    private val requirementControls = dataRepository.getAllRequirementData(
        RequirementData.TYPE, RequirementData::class.java
    ).map { requirements ->
        requirements.mapNotNull { item ->
            ControlConfig(
                item.componentName ?: return@mapNotNull null,
                item.controlId ?: return@mapNotNull null,
                item.smartspacerId,
                item.loadConfig
            )
        }
    }

    private val requestControls = combine(
        complicationControls,
        targetControls,
        requirementControls
    ) { complication, target, requirement ->
        complication + target + requirement
    }.map {
        it.groupBy { item -> item.componentName }.mapValues { item -> item.value.distinct() }
    }

    private val controlsConfig = combine(
        requestControls,
        shouldListen,
        listenOverride,
        reloadBus
    ) { request, listen, listenOverride, reload ->
        ControlsConfig(request, listen || listenOverride, reload)
    }.debounce(100L)

    private val loadedControls = controlsConfig.flatMapLatest {
        val request = it.requestControls
        val listen = it.listen
        if(request.isEmpty()) {
            //No controls to load!
            cachedState = emptyList()
            return@flatMapLatest flowOf(emptyList())
        }
        if(!listen) {
            return@flatMapLatest flowOf(cachedState)
        }
        val items = request.entries.map { item ->
            loadControls(item.key, item.value)
        }
        combine(*items.toTypedArray()) { controls ->
            controls.toList()
        }
    }.onEach {
        it.clearOverrides()
    }.stateIn(scope, SharingStarted.WhileSubscribed(), emptyList())

    private val controls = combine(
        loadedControls,
        controlOverrides.debounce(250L)
    ) { controls, overrides ->
        controls.map {
            it.applyOverrides(overrides)
        }
    }.onEach {
        cachedState = it.copyCached()
        SmartspacerComplicationProvider.notifyChange(context, ControlsComplication::class.java)
        SmartspacerTargetProvider.notifyChange(context, ControlsTarget::class.java)
        SmartspacerRequirementProvider.notifyChange(context, ControlsRequirement::class.java)
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    private suspend fun loadControls(
        componentName: ComponentName,
        controls: List<ControlConfig>
    ): Flow<ControlsAppState> {
        return subscribe(componentName, controls).map {
            ControlsAppState(componentName, it)
        }
    }

    override fun getControlsApps(): List<ControlsApp> {
        val intent = Intent(ACTION_CONTROLS_PROVIDER)
        return packageManager.queryIntentServicesCompat(intent).map {
            val label = it.loadLabel(packageManager)
            val componentName = ComponentName(it.serviceInfo.packageName, it.serviceInfo.name)
            val icon = Icon.createWithResource(componentName.packageName, it.iconResource)
            ControlsApp(icon, componentName, label)
        }.sortedBy {
            it.name.toString().lowercase()
        }
    }

    override suspend fun getControlsForApp(componentName: ComponentName): List<Control>? {
        return getAllControls(componentName).firstNotNull().unwrap()
    }

    override fun getControl(
        componentName: ComponentName,
        controlId: String,
        smartspacerId: String
    ): ControlState? {
        val app = controls.value.firstOrNull { it.componentName == componentName } ?: return null
        return app.state.firstOrNull {
            it.controlId == controlId && it.smartspacerId == smartspacerId
        }
    }

    override fun getControlAsFlow(
        componentName: ComponentName,
        controlId: String,
        smartspacerId: String
    ): Flow<ControlState?> {
        return controls.map {
            val app = it.firstOrNull { app -> app.componentName == componentName }
            app?.state?.firstOrNull { state ->
                state.controlId == controlId && state.smartspacerId == smartspacerId
            }
        }
    }

    override fun launchPanelIntent(componentName: ComponentName) {
        scope.launch {
            val panelIntent = getPanelIntent(componentName) ?: return@launch
            shizukuServiceRepository.runWithService {
                it.startActivity(panelIntent)
            }
        }
    }

    override fun getPanelIntent(componentName: ComponentName): Intent? {
        val intent = Intent().apply {
            component = componentName
        }
        val service = packageManager.resolveService(intent, PackageManager.GET_META_DATA)
            ?.serviceInfo?.metaData?.getString(META_DATA_PANEL_ACTIVITY) ?: return null
        val panelComponentName = try {
            ComponentName.unflattenFromString(service)
        }catch (e: Exception) {
            null
        } ?: return null
        return Intent().apply {
            component = panelComponentName
        }
    }

    override fun launchIntentAsRoot(intent: Intent) {
        scope.launch {
            shizukuServiceRepository.runWithService {
                it.startActivity(intent)
            }
        }
    }

    override fun showPowerMenu() {
        scope.launch {
            shizukuServiceRepository.runWithService {
                it.showPowerMenu()
            }
        }
    }

    override fun launchAppPendingIntent(pendingIntent: PendingIntent) {
        scope.launch {
            shizukuServiceRepository.runWithService {
                it.startPendingIntent(pendingIntent)
            }
        }
    }

    override fun setControlOverride(componentName: ComponentName, control: Control) {
        scope.launch {
            setControlOverride(
                componentName, control.controlId, ControlOverride.Control(componentName, control)
            )
        }
    }

    override fun runControlTapAction(
        action: ControlTapAction,
        controlExtraData: ControlExtraData,
        control: Control,
        componentName: ComponentName,
        smartspacerId: String,
        callback: (Int, ControlTapAction, ControlExtraData) -> Unit
    ) {
        val template = ControlsTemplate.getTemplate(control.controlTemplate)
        template.run {
            invokeTapAction(
                context,
                action,
                controlExtraData,
                control,
                componentName,
                smartspacerId,
                callback
            )
        }
    }

    override fun runControlAction(
        control: Control,
        componentName: ComponentName,
        controlAction: ControlAction,
        controlTapAction: ControlTapAction,
        controlExtraData: ControlExtraData,
        smartspacerId: String,
        resultCallback: (Int, ControlTapAction, ControlExtraData) -> Unit,
        callback: (Context, Control, ComponentName, ControlExtraData, Int, String, (Int, ControlTapAction, ControlExtraData) -> Unit) -> Unit
    ) {
        scope.launch {
            doRunControlAction(
                control,
                componentName,
                controlAction,
                controlTapAction,
                controlExtraData,
                false,
                smartspacerId,
                resultCallback,
                callback
            )
        }
    }

    private suspend fun doRunControlAction(
        control: Control,
        componentName: ComponentName,
        controlAction: ControlAction,
        controlTapAction: ControlTapAction,
        controlExtraData: ControlExtraData,
        isRetry: Boolean,
        smartspacerId: String,
        resultCallback: (Int, ControlTapAction, ControlExtraData) -> Unit,
        callback: (Context, Control, ComponentName, ControlExtraData, Int, String, (Int, ControlTapAction, ControlExtraData) -> Unit) -> Unit
    ) {
        val override = ControlOverride.SendingUpdate(componentName, control.controlId)
        val actionWithPasscode = controlAction.copyWithPasscode(controlExtraData.passcode)
        setControlOverride(componentName, control.controlId, override)
        if(startListeningAndWait(componentName, control.controlId)){
            //The control is ready to use!
            val result = mapControlsProvider(componentName, ignoreCache = true) {
                action(control.controlId, actionWithPasscode)
            }.first().unwrap() ?: ControlAction.RESPONSE_FAIL
            //If the action failed, clear the override since we won't get an update
            if(result != ControlAction.RESPONSE_OK) {
                if(!isRetry) {
                    //Reload and retry once, in case the control broke
                    reloadBus.emit(System.currentTimeMillis())
                    delay(2500L)
                    doRunControlAction(
                        control,
                        componentName,
                        actionWithPasscode,
                        controlTapAction,
                        controlExtraData,
                        true,
                        smartspacerId,
                        resultCallback,
                        callback
                    )
                    return
                }else{
                    setControlOverride(componentName, control.controlId, null)
                }
            }
            callback(
                context,
                control,
                componentName,
                controlExtraData,
                result,
                smartspacerId,
                resultCallback
            )
            resultCallback(result, controlTapAction, controlExtraData)
        }else{
            //Failed to load the control, clear the override for the user to try again
            setControlOverride(componentName, control.controlId, null)
        }
    }

    private suspend fun getAllControls(
        componentName: ComponentName
    ): Flow<ShizukuServiceResponse<List<Control>?>> {
        return mapControlsProvider(componentName) {
            getAllControls()
        }
    }

    private suspend fun subscribe(
        componentName: ComponentName,
        controls: List<ControlConfig>
    ): Flow<List<ControlState>> {
        val controlsArray = controls.toTypedArray()
        return mapControlsProvider(componentName) {
            subscribe(componentName, *controlsArray)
        }.onStart {
            emit(ShizukuServiceResponse.Success(
                ControlsState.Loading(componentName, controls)
            ))
        }.map {
            it.unwrap()?.applyControlConfig(controls) ?:
                controls.map { config ->
                    ControlState.Error(
                        componentName,
                        config.controlId,
                        config.smartspacerId,
                        getCachedIcon(componentName, config.controlId, config.smartspacerId),
                        getCachedTitle(componentName, config.controlId, config.smartspacerId)
                    )
                }
        }
    }

    /**
     *  Subscribes, waits for first Control or error and emits it. No further updates will be given.
     */
    override suspend fun subscribeOnce(
        componentName: ComponentName,
        controlId: String,
        smartspacerId: String
    ): Flow<Control?> = flow {
        val controls = listOf(
            ControlConfig(componentName, controlId, smartspacerId, LoadingConfig.LOADING)
        )
        subscribe(componentName, controls).mapNotNull {
            it.firstOrNull { control -> control.controlId == controlId }
        }.collect {
            when(it) {
                is ControlState.Control -> {
                    emit(it.control)
                }
                is ControlState.Error -> {
                    emit(null)
                }
                else -> {
                    //No-op
                }
            }
        }
    }.take(1)

    private fun ControlsState.applyControlConfig(
        controlConfigs: List<ControlConfig>
    ): List<ControlState> {
        val controls = when(this) {
            is ControlsState.Loading -> emptyList() //Allow config to be filled
            is ControlsState.Controls -> controls.map {
                val icon = it.value.getIcon(componentName)
                val title = it.value.title
                ControlState.Control(
                    componentName, 
                    it.value,
                    it.key.smartspacerId,
                    icon,
                    title
                )
            }
            is ControlsState.Error -> controlConfigs.map {
                ControlState.Error(
                    componentName, 
                    it.controlId,
                    it.smartspacerId,
                    getCachedIcon(componentName, it.controlId, it.smartspacerId),
                    getCachedTitle(componentName, it.controlId, it.smartspacerId)
                )
            }
        }
        return controlConfigs.map { control ->
            controls.firstOrNull {
                it.controlId == control.controlId && it.smartspacerId == control.smartspacerId
            } ?: applyControlConfig(controlConfigs, control.controlId, control.smartspacerId)
        }
    }

    private fun ControlsState.applyControlConfig(
        controlConfigs: List<ControlConfig>,
        controlId: String,
        smartspacerId: String
    ): ControlState {
        val config = controlConfigs.first {
            it.controlId == controlId
        }
        val cached = getCachedControl(componentName, controlId, smartspacerId)?.copyCached()
        val cachedIcon = cached?.let {
            (it as? ControlState.Control)?.control?.getIcon(componentName)
        }
        val cachedTitle = cached?.let {
            (it as? ControlState.Control)?.control?.title
        }
        return when(config.loadingConfig) {
            LoadingConfig.LOADING -> {
                ControlState.Loading(componentName, controlId, smartspacerId, cachedIcon, cachedTitle)
            }
            LoadingConfig.HIDDEN -> {
                ControlState.Hidden(componentName, controlId, smartspacerId, cachedIcon, cachedTitle)
            }
            LoadingConfig.CACHED -> {
                cached
                    ?: ControlState.Loading(componentName, controlId, smartspacerId, null, null)
            }
        }
    }

    private fun getCachedControl(
        componentName: ComponentName,
        controlId: String,
        smartspacerId: String
    ): ControlState? {
        return cachedState.firstOrNull { it.componentName == componentName }
            ?.state?.firstOrNull { it.controlId == controlId && it.smartspacerId == smartspacerId }
    }

    private fun Control.getIcon(componentName: ComponentName): Icon {
        val template = ControlsTemplate.getTemplate(controlTemplate)
        return template.run {
            getIcon(context, componentName)
        }
    }

    private fun getCachedIcon(
        componentName: ComponentName,
        smartspacerId: String,
        controlId: String
    ): Icon? {
        return getCachedControl(componentName, controlId, smartspacerId)?.let {
            (it as? ControlState.Control)?.control?.getIcon(componentName)
        }
    }

    private fun getCachedTitle(
        componentName: ComponentName,
        smartspacerId: String,
        controlId: String
    ): CharSequence? {
        return getCachedControl(componentName, controlId, smartspacerId)?.let {
            (it as? ControlState.Control)?.control?.title
        }
    }

    private suspend fun <T> mapControlsProvider(
        componentName: ComponentName,
        forceReconnect: Boolean = false,
        ignoreCache: Boolean = false,
        timeout: Long = SERVICE_TIMEOUT,
        block: suspend IControlsProvider.() -> Flow<T>
    ): Flow<ShizukuServiceResponse<T?>> {
        val intent = Intent(ACTION_CONTROLS_PROVIDER).apply {
            component = componentName
        }
        return getControlsProvider(intent, forceReconnect, ignoreCache).flatMapLatest {
            when {
                it is ShizukuServiceResponse.Success && it.result != null -> {
                    block(it.result).timeoutFirst(timeout).map { state ->
                        ShizukuServiceResponse.Success(state)
                    }
                }
                it is ShizukuServiceResponse.Failed -> {
                    flowOf(ShizukuServiceResponse.Failed(it.reason))
                }
                else -> {
                    flowOf(ShizukuServiceResponse.Failed(FailureReason.NOT_AVAILABLE))
                }
            }
        }.flatMapLatest {
            if(it is ShizukuServiceResponse.Success && it.result == null && !forceReconnect) {
                mapControlsProvider(
                    componentName,
                    true,
                    ignoreCache,
                    timeout,
                    block
                )
            }else flowOf(it)
        }
    }

    private suspend fun getControlsProvider(
        intent: Intent,
        forceReconnect: Boolean,
        ignoreCache: Boolean
    ): Flow<ShizukuServiceResponse<IControlsProvider?>?> = serviceLock.withLock {
        val component = intent.resolveService(context) ?: return flowOf(null)
        if(forceReconnect) {
            shizukuServiceRepository.runWithService {
                it.forceStop(component.packageName, Process.myUserHandle().getIdentifier())
            }
        }else if(!ignoreCache){
            controlsProviders[component]?.let {
                return it
            }
        }
        return shizukuServiceRepository.suiService.flatMapLatest {
            if(it == null) {
                return@flatMapLatest flowOf(
                    ShizukuServiceResponse.Failed(FailureReason.NOT_AVAILABLE)
                )
            }
            val controlsIntent = intent.makeControlsIntent()
            it.getControlsProvider(context, controlsIntent).map { provider ->
                ShizukuServiceResponse.Success(provider)
            }
        }.shareIn(scope, SharingStarted.WhileSubscribed()).onCompletion {
            controlsProviders.remove(component)
        }.also {
            controlsProviders[component] = it
        }
    }

    private suspend fun setControlOverride(
        componentName: ComponentName,
        controlId: String,
        controlOverride: ControlOverride?
    ) = controlOverridesLock.withLock {
        val current = controlOverrides.value.toMutableSet()
        //Remove any existing for this Control
        current.removeIf {
            it.controlId == controlId && it.componentName == componentName
        }
        if(controlOverride != null) {
            current.add(controlOverride)
        }
        controlOverrides.emit(current)
    }

    private fun ControlsAppState.applyOverrides(overrides: Set<ControlOverride>): ControlsAppState {
        return copy(state = state.map {
            if(it !is ControlState.Control) return@map it
            val override = overrides.firstOrNull { o ->
                o.controlId == it.controlId && o.componentName == it.componentName
            } ?: return@map it
            when(override) {
                is ControlOverride.Control -> it.copy(control = override.control)
                is ControlOverride.SendingUpdate -> {
                    ControlState.Sending(
                        it.componentName,
                        it.controlId,
                        it.smartspacerId,
                        it.cachedIcon,
                        it.cachedTitle
                    )
                }
            }
        })
    }

    private suspend fun List<ControlsAppState>.clearOverrides() = controlOverridesLock.withLock {
        val current = controlOverrides.value.toMutableSet()
        forEach { app ->
            app.state.forEach {
                current.removeIf { c ->
                    c.controlId == it.controlId && c.componentName == it.componentName &&
                            it.loadedAt >= c.overriddenAt //Only remove for newer Controls
                }
            }
        }
        controlOverrides.emit(current)
    }

    override fun startListeningFromWake() {
        if(settingsRepository.refreshOnScreenStateChanged.getSync()) {
            startListening()
        }
    }

    @Synchronized
    override fun startListening() {
        subscribeJob?.cancel()
        subscribeJob = scope.launch {
            if(!shouldListen.value) {
                reloadBus.emit(System.currentTimeMillis())
            }
            shouldListen.emit(true)
            //null = infinite, should never stop listening
            val delayDuration = settingsRepository.refreshPeriod.get().duration?.toMillis()
                ?: return@launch
            delay(delayDuration)
            shouldListen.emit(false)
            subscribeJob = null
        }
    }

    @Synchronized
    override fun stopListening() {
        subscribeJob?.cancel()
        subscribeJob = null
        scope.launch {
            shouldListen.emit(false)
        }
    }

    override fun overrideListening(listen: Boolean) {
        scope.launch {
            listenOverride.emit(listen)
        }
    }

    private suspend fun startListeningAndWait(
        componentName: ComponentName,
        controlId: String
    ): Boolean {
        startListening()
        return withTimeoutOrNull(LISTEN_TIMEOUT) {
            loadedControls.firstOrNull {
                it.containsWithoutCache(componentName, controlId)
            } != null
        } ?: false
    }

    private fun List<ControlsAppState>.containsWithoutCache(
        componentName: ComponentName,
        controlId: String
    ): Boolean {
        return any { app ->
            app.state.any { state ->
                (state is ControlState.Control && state.componentName == componentName
                        && state.controlId == controlId && !state.loadedFromCache)
            }
        }
    }

    private fun List<ControlsAppState>.copyCached(): List<ControlsAppState> {
        return map { app ->
            app.copy(state = app.state.map {
                it.copyCached()
            })
        }
    }

    private fun ControlAction.copyWithPasscode(passcode: String?): ControlAction {
        return when(this) {
            is BooleanAction -> BooleanAction(templateId, newState, passcode)
            is FloatAction -> FloatAction(templateId, newValue, passcode)
            is ModeAction -> ModeAction(templateId, newMode, passcode)
            else -> this
        }
    }

    private data class ControlsAppState(
        val componentName: ComponentName,
        val state: List<ControlState>
    )

    private data class ControlsConfig(
        val requestControls: Map<ComponentName, List<ControlConfig>>,
        val listen: Boolean,
        val reloadTime: Long
    )

    private sealed class ControlOverride(
        open val componentName: ComponentName,
        open val controlId: String,
        val overriddenAt: Long = System.currentTimeMillis()
    ) {

        data class SendingUpdate(
            override val componentName: ComponentName,
            override val controlId: String
        ): ControlOverride(componentName, controlId)

        data class Control(
            override val componentName: ComponentName,
            val control: android.service.controls.Control
        ): ControlOverride(componentName, control.controlId)
    }

}