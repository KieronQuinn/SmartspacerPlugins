package com.kieronquinn.app.smartspacer.plugin.energymonitor

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.energymonitor.repositories.StateRepository
import com.kieronquinn.app.smartspacer.plugin.energymonitor.repositories.StateRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.SmartspacerPlugin
import org.koin.dsl.module

class EnergyMonitorPlugin: SmartspacerPlugin() {

    companion object {
        const val PACKAGE_NAME = "strange.watch.longevity.ion"
    }

    override fun getModule(context: Context) = module {
        single<StateRepository> { StateRepositoryImpl(get()) }
    }

}