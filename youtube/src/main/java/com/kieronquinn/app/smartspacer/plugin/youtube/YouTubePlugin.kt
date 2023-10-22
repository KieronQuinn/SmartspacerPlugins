package com.kieronquinn.app.smartspacer.plugin.youtube

import android.content.Context
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import com.kieronquinn.app.smartspacer.plugin.youtube.adapters.SubscriberCountAdapter
import com.kieronquinn.app.smartspacer.plugin.youtube.model.database.YouTubeDatabase
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeRepository
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeRepository.SubscriberCount
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeSettingsRepository
import com.kieronquinn.app.smartspacer.plugin.youtube.repositories.YouTubeSettingsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.youtube.service.YouTubeService
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.SubscriptionsConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.SubscriptionsConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.apikey.ApiKeyViewModel
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.apikey.ApiKeyViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.channelid.ChannelIdViewModel
import com.kieronquinn.app.smartspacer.plugin.youtube.ui.screens.subscriptions.channelid.ChannelIdViewModelImpl
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class YouTubePlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        single<YouTubeSettingsRepository> { YouTubeSettingsRepositoryImpl(get() ) }
        single { YouTubeService.createService() }
        single { YouTubeDatabase.getDatabase(get()) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<YouTubeRepository> { YouTubeRepositoryImpl(get(), get(), get(), get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
        viewModel<SubscriptionsConfigurationViewModel> { SubscriptionsConfigurationViewModelImpl(get(), get(), get(), get()) }
        viewModel<ChannelIdViewModel> { ChannelIdViewModelImpl(get()) }
        viewModel<ApiKeyViewModel> { ApiKeyViewModelImpl(get()) }
    }

    override fun GsonBuilder.configure() = apply {
        registerTypeAdapter(SubscriberCount::class.java, SubscriberCountAdapter)
    }

}