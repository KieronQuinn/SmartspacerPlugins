package com.kieronquinn.app.smartspacer.plugin.countdown

import android.content.Context
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.countdown.adapters.IconAdapter
import com.kieronquinn.app.smartspacer.plugin.countdown.model.Icon
import com.kieronquinn.app.smartspacer.plugin.countdown.model.database.CountdownDatabase
import com.kieronquinn.app.smartspacer.plugin.countdown.repositories.AlarmRepository
import com.kieronquinn.app.smartspacer.plugin.countdown.repositories.AlarmRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.countdown.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.countdown.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.configuration.ConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.configuration.ConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.icon.IconPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.icon.IconPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.icon.file.FilePickerViewModel
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.icon.file.FilePickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.icon.font.FontPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.icon.font.FontPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.icon.font.picker.FontIconPickerViewModel
import com.kieronquinn.app.smartspacer.plugin.countdown.ui.screens.icon.font.picker.FontIconPickerViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class CountdownPlugin: SmartspacerPlugin() {

    override fun getModule(context: Context) = module {
        viewModel<ConfigurationViewModel> { ConfigurationViewModelImpl(get(), get(), get()) }
        viewModel<IconPickerViewModel> { IconPickerViewModelImpl(get()) }
        viewModel<FilePickerViewModel> { FilePickerViewModelImpl(get()) }
        viewModel<FontPickerViewModel> { FontPickerViewModelImpl(get()) }
        viewModel<FontIconPickerViewModel> { FontIconPickerViewModelImpl(get()) }
        single<AlarmRepository>(createdAtStart = true) { AlarmRepositoryImpl(get()) }
        single { CountdownDatabase.getDatabase(get()) }
        single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
        single<NavGraphRepository> { NavGraphRepositoryImpl() }
    }

    override fun GsonBuilder.configure() = apply {
        registerTypeAdapter(Icon::class.java, IconAdapter)
    }

}