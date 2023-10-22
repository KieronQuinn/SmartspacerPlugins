package com.kieronquinn.app.smartspacer.plugins.pokemongo

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugins.pokemongo.model.database.PokemonGoDatabase
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepository
import com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories.WidgetRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.pokemongo.ui.screens.configuration.ConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugins.pokemongo.ui.screens.configuration.ConfigurationViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository as SharedDatabaseRepository

class PokemonGoPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context): Module {
        return module {
            single<WidgetRepository> { WidgetRepositoryImpl(get()) }
            single { PokemonGoDatabase.getDatabase(context) }
            single<SharedDatabaseRepository> { DatabaseRepository(get()) }
            single<NavGraphRepository> { NavGraphRepositoryImpl() }
            viewModel<ConfigurationViewModel> { ConfigurationViewModelImpl(get(), get()) }
        }
    }

    enum class Variant(val packageName: String) {
        PLAY("com.nianticlabs.pokemongo"),
        SAMSUNG("com.nianticlabs.pokemongo.ares")
    }

}