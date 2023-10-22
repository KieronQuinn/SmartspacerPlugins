package com.kieronquinn.app.smartspacer.plugins.datausage

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugins.datausage.model.database.DataUsageDatabase
import com.kieronquinn.app.smartspacer.plugins.datausage.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.datausage.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.datausage.ui.screens.configuration.ConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugins.datausage.ui.screens.configuration.ConfigurationViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class DataUsagePlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single { DataUsageDatabase.getDatabase(get()) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<ConfigurationViewModel> { ConfigurationViewModelImpl(get(), get(), get()) }
    }

}