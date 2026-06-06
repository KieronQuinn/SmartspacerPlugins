package com.kieronquinn.app.smartspacer.plugin.googlehealth

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.database.GoogleHealthDatabase
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.GoogleHealthRepository
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.GoogleHealthRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens.HealthConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens.HealthConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens.focus.HealthConfigurationFocusViewModel
import com.kieronquinn.app.smartspacer.plugin.googlehealth.ui.screens.focus.HealthConfigurationFocusViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.lsposed.hiddenapibypass.HiddenApiBypass
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository as SharedDatabaseRepository

class GoogleHealthPlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "com.fitbit.FitbitMobile"
    }

    override fun attachBaseContext(base: Context) {
        HiddenApiBypass.addHiddenApiExemptions("")
        super.attachBaseContext(base)
    }

    override fun getModule(context: Context) = module {
        single { GoogleHealthDatabase.getDatabase(get()) }
        single<DatabaseRepository> { DatabaseRepository(get()) } bind SharedDatabaseRepository::class
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single<GoogleHealthRepository> { GoogleHealthRepositoryImpl(get(), get()) }
        viewModel<HealthConfigurationViewModel> { HealthConfigurationViewModelImpl(get(), get(), get()) }
        viewModel<HealthConfigurationFocusViewModel> { HealthConfigurationFocusViewModelImpl(get(), get()) }
    }

}