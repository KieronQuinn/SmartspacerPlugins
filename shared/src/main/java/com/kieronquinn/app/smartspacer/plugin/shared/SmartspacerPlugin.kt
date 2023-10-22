package com.kieronquinn.app.smartspacer.plugin.shared

import android.app.Application
import android.content.Context
import androidx.annotation.CallSuper
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.shared.components.blur.BlurProvider
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.ContainerNavigationImpl
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.RootNavigation
import com.kieronquinn.app.smartspacer.plugin.shared.components.navigation.RootNavigationImpl
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DataRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.RoomEncryptedSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.RoomEncryptedSettingsRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

abstract class SmartspacerPlugin: Application() {

    @CallSuper
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        startKoin {
            androidContext(base)
            modules(sharedModule(), getModule(base))
        }
        base.configure()
    }

    private fun sharedModule() = module {
        single { createGson() }
        single { BlurProvider.getBlurProvider(resources) }
        single<RootNavigation> { RootNavigationImpl() }
        single<ContainerNavigation> { ContainerNavigationImpl() }
        single<RoomEncryptedSettingsRepository> { RoomEncryptedSettingsRepositoryImpl(get(), getOrNull()) }
        single<DataRepository> { DataRepositoryImpl(get(), get(), get())}
    }

    private fun createGson(): Gson {
        return GsonBuilder()
            .configure()
            .create()
    }

    open fun GsonBuilder.configure(): GsonBuilder = this
    open fun Context.configure() {}

    abstract fun getModule(context: Context): Module

}