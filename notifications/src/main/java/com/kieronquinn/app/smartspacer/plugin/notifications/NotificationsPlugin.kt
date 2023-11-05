package com.kieronquinn.app.smartspacer.plugin.notifications

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.TelegramRepository
import com.kieronquinn.app.smartspacer.plugin.notifications.repositories.TelegramRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration.ConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.notifications.ui.screens.configuration.ConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class NotificationsPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<TelegramRepository> { TelegramRepositoryImpl(get()) }
        viewModel<ConfigurationViewModel> { ConfigurationViewModelImpl(get(), it.get())}
    }

}