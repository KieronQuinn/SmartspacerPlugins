package com.kieronquinn.app.smartspacer.plugin.googlehome

import android.content.Context
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.googlehome.model.database.GoogleHomeDatabase
import com.kieronquinn.app.smartspacer.plugin.googlehome.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.googlehome.repositories.GoogleHomeRepository
import com.kieronquinn.app.smartspacer.plugin.googlehome.repositories.GoogleHomeRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlehome.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlehome.ui.screens.complication.GoogleHomeComplicationConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.googlehome.ui.screens.complication.GoogleHomeComplicationConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.googlehome.ui.screens.target.GoogleHomeTargetConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.googlehome.ui.screens.target.GoogleHomeTargetConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import org.lsposed.hiddenapibypass.HiddenApiBypass
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository as SharedDatabaseRepository

class GoogleHomePlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "com.google.android.apps.chromecast.app"

        fun getGoogleHomeContext(context: Context): Context? {
            return try {
                context.createPackageContext(
                    PACKAGE_NAME,
                    CONTEXT_INCLUDE_CODE or CONTEXT_IGNORE_SECURITY
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        HiddenApiBypass.addHiddenApiExemptions("")
        super.attachBaseContext(base)
    }

    override fun getModule(context: Context) = module {
        single { Gson() }
        single { GoogleHomeDatabase.getDatabase(get()) }
        single<DatabaseRepository> { DatabaseRepository(get()) } bind SharedDatabaseRepository::class
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single<GoogleHomeRepository> { GoogleHomeRepositoryImpl(get(), get(), get()) }
        viewModel<GoogleHomeTargetConfigurationViewModel> { GoogleHomeTargetConfigurationViewModelImpl(get()) }
        viewModel<GoogleHomeComplicationConfigurationViewModel> { GoogleHomeComplicationConfigurationViewModelImpl(get()) }
    }

}