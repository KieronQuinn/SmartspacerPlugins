package com.kieronquinn.app.smartspacer.plugin.aftership

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.aftership.model.database.AftershipDatabase
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.AftershipRepository
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.AftershipRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.TrackingRepository
import com.kieronquinn.app.smartspacer.plugin.aftership.repositories.TrackingRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.aftership.ui.screens.configuration.AftershipTargetConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.aftership.ui.screens.configuration.AftershipTargetConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository as SharedDatabaseRepository

class AftershipPlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "com.aftership.AfterShip"
    }

    override fun getModule(context: Context): Module {
        return module {
            single<TrackingRepository> { TrackingRepositoryImpl(context) }
            single<AftershipRepository>(createdAtStart = true) {
                AftershipRepositoryImpl(context, get(), get())
            }
            single<NavGraphRepository> { NavGraphRepositoryImpl() }
            single { AftershipDatabase.getDatabase(context) }
            single<SharedDatabaseRepository> { DatabaseRepositoryImpl(get()) }
            single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
            viewModel<AftershipTargetConfigurationViewModel> {
                AftershipTargetConfigurationViewModelImpl(get(), get())
            }
        }
    }

}