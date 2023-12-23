package com.kieronquinn.app.smartspacer.plugin.googlemaps

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.googlemaps.model.database.GoogleMapsDatabase
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlemaps.ui.screens.configuration.GoogleMapsTrafficConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.googlemaps.ui.screens.configuration.GoogleMapsTrafficConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class GoogleMapsPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context): Module {
        return module {
            single { GoogleMapsDatabase.getDatabase(get()) }
            single<GoogleMapsRepository> { GoogleMapsRepositoryImpl(get(), get()) }
            single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
            single<NavGraphRepository> { NavGraphRepositoryImpl() }
            viewModel<GoogleMapsTrafficConfigurationViewModel> {
                GoogleMapsTrafficConfigurationViewModelImpl(get(), get())
            }
        }
    }

}