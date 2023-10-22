package com.kieronquinn.app.smartspacer.plugins.googlemaps.utils

import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.CallSuper
import androidx.test.platform.app.InstrumentationRegistry
import com.kieronquinn.app.smartspacer.plugin.googlemaps.BuildConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestRule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.koin.core.component.KoinComponent
import com.kieronquinn.app.smartspacer.plugin.googlemaps.test.BuildConfig as TestBuildConfig


abstract class BaseTest<SUT>: KoinComponent {

    companion object {
        @JvmStatic
        protected inline fun <reified R : Any> mock(
            block: R.() -> Unit = {}
        ) = mockk<R>(relaxed = true) {
            block(this)
        }
    }

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    val scope = CoroutineScope(mainDispatcherRule.testDispatcher)

    @get:Rule
    var watcher: TestRule = object : TestWatcher() {
        override fun starting(description: Description) {
            Log.d("SmartspacerTests", "Starting test: " + description.methodName)
        }
    }

    private val settingsSecureMap = HashMap<String, String>()
    protected val settingsGlobalMap = HashMap<String, String>()

    protected val actualContext: Context by lazy {
        InstrumentationRegistry.getInstrumentation().targetContext
    }

    protected val testContext: Context by lazy {
        InstrumentationRegistry.getInstrumentation().context
    }

    private val broadcastContext = mock<ContextWrapper> {
        val looper = Looper.getMainLooper()
        every { this@mock.applicationContext } returns this
        every { this@mock.mainLooper } returns looper
        every { this@mock.contentResolver } returns actualContext.contentResolver
    }

    private val broadcastManager by lazy {
        TestBroadcastManager()
    }

    protected val contentResolver by lazy {
        TestContentResolver()
    }

    protected val resourcesMock by lazy {
        mock<Resources>()
    }

    protected val packageManagerMock by lazy {
        mock<PackageManager>()
    }

    protected val contentResolverMock by lazy {
        mockk<ContentResolver> {
            every { notifyChange(any(), any()) } answers {
                contentResolver.notifyChange(firstArg())
            }
            every { notifyChange(any<Uri>(), any(), any<Int>()) } answers {
                contentResolver.notifyChange(firstArg())
            }
        }
    }

    protected val contextMock by lazy {
        mock<Context> {
            every { resources } returns resourcesMock
            every { packageManager } returns packageManagerMock
            every { contentResolver } returns contentResolverMock
            every { packageName } returns BuildConfig.APPLICATION_ID
            every { opPackageName } returns BuildConfig.APPLICATION_ID
            every { applicationContext } returns this
            setupBroadcastManager()
            context()
        }
    }

    abstract val sut: SUT

    open fun Context.context() {
        //No-op by default
    }

    @Before
    @CallSuper
    open fun setup() {
        //APPLICATION_ID is used throughout the app which breaks some calls. Force it to be .test
        BuildConfig::class.java.getDeclaredField("APPLICATION_ID").apply {
            isAccessible = true
        }.set(null, TestBuildConfig.APPLICATION_ID)
        mockSettingsSecure()
        mockSettingsGlobal()
        TestUtils.registerContentObserver = { uri, observer ->
            contentResolver.registerContentObserver(uri, true, observer)
        }
        TestUtils.unregisterContentObserver = {
            contentResolver.unregisterContentObserver(it)
        }
    }

    private fun Context.setupBroadcastManager() {
        //Set up broadcast manager
        val receiverCapture = slot<BroadcastReceiver>()
        val filterCapture = slot<IntentFilter>()
        every { registerReceiver(capture(receiverCapture), capture(filterCapture)) } answers {
            broadcastManager.registerReceiver(receiverCapture.captured, filterCapture.captured)
            Intent()
        }
        val unregisterReceiverCapture = slot<BroadcastReceiver>()
        every { unregisterReceiver(capture(unregisterReceiverCapture)) } answers {
            broadcastManager.unregisterReceiver(unregisterReceiverCapture.captured)
        }
        val intentCapture = slot<Intent>()
        every { sendBroadcast(capture(intentCapture)) } answers {
            broadcastManager.sendBroadcast(intentCapture.captured)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    class MainDispatcherRule(
        val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
    ) : TestWatcher() {
        override fun starting(description: Description) {
            Dispatchers.setMain(testDispatcher)
            super.starting(description)
        }

        override fun finished(description: Description) {
            Dispatchers.resetMain()
            super.finished(description)
        }
    }

    private fun mockSettingsSecure() = mockkStatic(Settings.Secure::class).apply {
        every { Settings.Secure.getString(any(), any()) } answers  {
            settingsSecureMap[secondArg()] ?: ""
        }
    }

    private fun mockSettingsGlobal() = mockkStatic(Settings.Global::class).apply {
        every { Settings.Global.getString(any(), any()) } answers  {
            settingsGlobalMap[secondArg()] ?: ""
        }
        every { Settings.Global.getInt(any(), any()) } answers  {
            (settingsGlobalMap[secondArg()] ?: "0").toInt()
        }
    }

}