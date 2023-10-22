package com.kieronquinn.app.smartspacer.plugins.battery

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.BitmapTypeAdapter
import com.kieronquinn.app.smartspacer.plugins.battery.model.database.BatteryDatabase
import com.kieronquinn.app.smartspacer.plugins.battery.repositories.BatteryRepository
import com.kieronquinn.app.smartspacer.plugins.battery.repositories.BatteryRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.battery.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.battery.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.battery.ui.screens.configuration.ConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugins.battery.ui.screens.configuration.ConfigurationViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class BatteryPlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "com.google.android.settings.intelligence"
    }

    override fun getModule(context: Context) = module {
        single { BatteryDatabase.getDatabase(get()) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<BatteryRepository> { BatteryRepositoryImpl(get(), get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<ConfigurationViewModel> { ConfigurationViewModelImpl(get(), get()) }
    }

    override fun GsonBuilder.configure() = apply {
        registerTypeAdapter(Bitmap::class.java, BitmapTypeAdapter())
    }

}