package com.kieronquinn.app.smartspacer.plugins.yahoosport

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugins.yahoosport.model.database.YahooSportDatabase
import com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories.GameRepository
import com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories.GameRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.yahoosport.ui.screens.configuration.ConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugins.yahoosport.ui.screens.configuration.ConfigurationViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class YahooSportPlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "com.yahoo.mobile.client.android.sportacular"
    }

    override fun getModule(context: Context) = module {
        single<GameRepository> { GameRepositoryImpl(get(), get()) }
        single { YahooSportDatabase.getDatabase(get()) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<ConfigurationViewModel> { ConfigurationViewModelImpl(get(), get()) }
    }

}