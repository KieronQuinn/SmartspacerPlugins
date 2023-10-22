package com.kieronquinn.app.smartspacer.plugins.battery.repositories

import android.content.Context
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugins.battery.complications.BatteryComplication
import com.kieronquinn.app.smartspacer.plugins.battery.model.BatteryLevels
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

interface BatteryRepository {

    val batteryLevelsChanged: StateFlow<Long>

    fun setBatteryLevels(batteryLevels: BatteryLevels)
    fun getBatteryLevels(): BatteryLevels?
    fun getBatteryLevel(name: String): BatteryLevels.BatteryLevel?

}

class BatteryRepositoryImpl(
    private val gson: Gson,
    private val context: Context
): BatteryRepository {

    private val batteryLevelFile = File(context.filesDir, "levels.json").also {
        it.parentFile?.mkdirs()
    }

    private val scope = MainScope()
    private val writeLock = Mutex()

    override val batteryLevelsChanged = MutableStateFlow(System.currentTimeMillis())

    override fun setBatteryLevels(batteryLevels: BatteryLevels) {
        scope.launch(Dispatchers.IO) {
            writeLock.withLock {
                val json = gson.toJson(batteryLevels)
                batteryLevelFile.writeText(json)
            }
            batteryLevelsChanged.emit(System.currentTimeMillis())
            SmartspacerComplicationProvider.notifyChange(context, BatteryComplication::class.java)
        }
    }

    override fun getBatteryLevels(): BatteryLevels? {
        return try {
            gson.fromJson(batteryLevelFile.readText(), BatteryLevels::class.java)
        }catch (e: Exception){
            null
        }
    }

    override fun getBatteryLevel(name: String): BatteryLevels.BatteryLevel? {
        return getBatteryLevels()?.levels?.firstOrNull {
            it.name == name
        }
    }

}