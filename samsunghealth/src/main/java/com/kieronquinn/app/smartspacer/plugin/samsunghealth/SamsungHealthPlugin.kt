package com.kieronquinn.app.smartspacer.plugin.samsunghealth

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.model.database.SamsungHealthDatabase
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories.SamsungHealthSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories.SamsungHealthSettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.ui.screens.configuration.sleep.SleepConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.samsunghealth.ui.screens.configuration.sleep.SleepConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository as SharedDatabaseRepository

class SamsungHealthPlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "com.sec.android.app.shealth"
    }

    override fun getModule(context: Context) = module {
        single { SamsungHealthDatabase.getDatabase(get()) }
        single<SharedDatabaseRepository> { DatabaseRepository(get()) }
        single<SamsungHealthSettingsRepository> { SamsungHealthSettingsRepositoryImpl(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<SleepConfigurationViewModel> { SleepConfigurationViewModelImpl(get()) }
    }

}