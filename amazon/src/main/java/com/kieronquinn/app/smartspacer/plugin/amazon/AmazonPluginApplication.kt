package com.kieronquinn.app.smartspacer.plugin.amazon

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.amazon.model.database.AmazonDatabase
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.AmazonSettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.configuration.AmazonTargetConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.amazon.ui.screens.configuration.AmazonTargetConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class AmazonPluginApplication: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME_GLOBAL = "com.amazon.mShop.android.shopping"
        const val PACKAGE_NAME_INDIA = "in.amazon.mShop.android.shopping"
    }

    override fun getModule(context: Context): Module = module {
        single { AmazonDatabase.getDatabase(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<AmazonRepository> { AmazonRepositoryImpl(get(), get(), get()) }
        single<AmazonSettingsRepository> { AmazonSettingsRepositoryImpl(get()) }
        viewModel<AmazonTargetConfigurationViewModel> { AmazonTargetConfigurationViewModelImpl(get(), get(), get()) }
    }

}