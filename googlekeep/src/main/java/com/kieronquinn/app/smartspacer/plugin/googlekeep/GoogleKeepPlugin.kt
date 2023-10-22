package com.kieronquinn.app.smartspacer.plugin.googlekeep

import android.content.Context
import com.google.gson.GsonBuilder
import com.kieronquinn.app.smartspacer.plugin.googlekeep.adapters.NoteAdapter
import com.kieronquinn.app.smartspacer.plugin.googlekeep.model.Note
import com.kieronquinn.app.smartspacer.plugin.googlekeep.model.database.GoogleKeepDatabase
import com.kieronquinn.app.smartspacer.plugin.googlekeep.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlekeep.repositories.KeepRepository
import com.kieronquinn.app.smartspacer.plugin.googlekeep.repositories.KeepRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlekeep.repositories.NavGraphRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlekeep.ui.screens.configuration.ConfigurationViewModel
import com.kieronquinn.app.smartspacer.plugin.googlekeep.ui.screens.configuration.ConfigurationViewModelImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepository
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.NavGraphRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

class GoogleKeepPlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "com.google.android.keep"
    }

    override fun getModule(context: Context): Module {
        return module {
            single { GoogleKeepDatabase.getDatabase(get()) }
            single<DatabaseRepository> { DatabaseRepositoryImpl(get()) }
            single<NavGraphRepository> { NavGraphRepositoryImpl() }
            single<KeepRepository> { KeepRepositoryImpl(get()) }
            viewModel<ConfigurationViewModel> { ConfigurationViewModelImpl(get()) }
        }
    }

    override fun GsonBuilder.configure() = apply {
        registerTypeAdapter(Note::class.java, NoteAdapter)
    }

}