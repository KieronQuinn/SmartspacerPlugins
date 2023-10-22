package com.kieronquinn.app.smartspacer.plugin.uber

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.isPackageInstalled
import com.kieronquinn.app.smartspacer.plugin.uber.model.database.UberDatabase
import com.kieronquinn.app.smartspacer.plugin.uber.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.uber.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.uber.repositories.NotificationRepository
import com.kieronquinn.app.smartspacer.plugin.uber.repositories.NotificationRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.uber.ui.screens.configuration.ConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.uber.ui.screens.configuration.ConfigurationViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class UberPlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "com.ubercab"

        fun isAppInstalled(context: Context): Boolean {
            return context.packageManager.isPackageInstalled(PACKAGE_NAME)
        }
    }

    override fun getModule(context: Context): Module {
        return module {
            single<NotificationRepository> { NotificationRepositoryImpl(get()) }
            single { UberDatabase.getDatabase(get()) }
            single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
            single<NavGraphRepository> { NavGraphRepositoryImpl() }
            viewModel<ConfigurationViewModel> { ConfigurationViewModelImpl(get()) }
        }
    }

}