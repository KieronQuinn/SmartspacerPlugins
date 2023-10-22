package com.kieronquinn.app.smartspacer.plugin.healthconnect

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.database.HealthConnectDatabase
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.HealthConnectRepository
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.HealthConnectRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.configuration.ConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.configuration.ConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.setup.SetupViewModel
import com.kieronquinn.app.smartspacer.plugin.healthconnect.ui.screens.setup.SetupViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository as SharedDatabaseRepository

class HealthConnectPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single { HealthConnectDatabase.getDatabase(get()) }
        single<SharedDatabaseRepository> { DatabaseRepositoryImpl(get(), get()) }
        single<DatabaseRepository> { get<SharedDatabaseRepository>() as DatabaseRepositoryImpl }
        single<HealthConnectRepository> { HealthConnectRepositoryImpl(get(), get(), get()) }
        single<Gson> { GsonBuilder().create() }
        viewModel<SetupViewModel> { SetupViewModelImpl(get(), get(), get(), get()) }
        viewModel<ConfigurationViewModel> { ConfigurationViewModelImpl(get(), get(), get(), get()) }
    }

    override fun GsonBuilder.configure() = apply {
    }

}