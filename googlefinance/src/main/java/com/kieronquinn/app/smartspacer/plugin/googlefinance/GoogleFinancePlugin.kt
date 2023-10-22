package com.kieronquinn.app.smartspacer.plugin.googlefinance

import android.content.Context
import android.graphics.Bitmap
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.googlefinance.model.database.GoogleFinanceDatabase
import com.kieronquinn.app.smartspacer.plugin.googlefinance.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlefinance.repositories.GoogleFinanceRepository
import com.kieronquinn.app.smartspacer.plugin.googlefinance.repositories.GoogleFinanceRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlefinance.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlefinance.ui.screens.configuration.ConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.googlefinance.ui.screens.configuration.ConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.googlefinance.ui.screens.configuration.minimumtrend.MinimumTrendViewModel
import com.kieronquinn.app.smartspacer.plugin.googlefinance.ui.screens.configuration.minimumtrend.MinimumTrendViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.shared.utils.gson.BitmapTypeAdapter
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class GoogleFinancePlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "com.google.android.googlequicksearchbox"
    }

    override fun getModule(context: Context) = module {
        single { GoogleFinanceDatabase.getDatabase(get()) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single<GoogleFinanceRepository> { GoogleFinanceRepositoryImpl(get(), get()) }
        viewModel<ConfigurationViewModel> { ConfigurationViewModelImpl(get(), get()) }
        viewModel<MinimumTrendViewModel> { MinimumTrendViewModelImpl(get()) }
    }

    override fun GsonBuilder.configure() = apply {
        registerTypeAdapter(Bitmap::class.java, BitmapTypeAdapter())
    }

}