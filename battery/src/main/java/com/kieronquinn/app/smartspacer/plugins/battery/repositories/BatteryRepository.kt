package com.kieronquinn.app.smartspacer.plugins.battery.repositories

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.firstNotNull
import com.kieronquinn.app.smartspacer.plugins.battery.complications.BatteryComplication
import com.kieronquinn.app.smartspacer.plugins.battery.model.BatteryLevels
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File

interface BatteryRepository {

    fun setBatteryLevels(batteryLevels: BatteryLevels)
    fun getBatteryLevels(): Flow<BatteryLevels>
    fun getBatteryLevel(name: String): BatteryLevels.BatteryLevel?

}

class BatteryRepositoryImpl(
    context: Context,
    private val databaseRepository: DatabaseRepository
): BatteryRepository {

    private val scope = MainScope()
    private val updateLock = Mutex()

    private val cacheDir by lazy {
        File(context.cacheDir, "icons").apply {
            mkdirs()
        }
    }

    private val cachedBatteryLevels = databaseRepository.getCachedBatteryLevels()
        .onEach {
            SmartspacerComplicationProvider.notifyChange(context, BatteryComplication::class.java)
        }.stateIn(scope, SharingStarted.Eagerly, null)

    override fun setBatteryLevels(batteryLevels: BatteryLevels) {
        scope.launch {
            updateLock.withLock {
                val current = cachedBatteryLevels.firstNotNull()
                batteryLevels.levels.forEach {
                    val cached = it.toCachedBatteryLevel(cacheDir)
                    databaseRepository.setCachedBatteryLevel(cached)
                }
                val disconnected = current.filter {
                    batteryLevels.levels.none { level ->
                        level.name == it.name
                    }
                }
                disconnected.forEach {
                    databaseRepository.setCachedBatteryLevelConnected(it.name,false)
                }
            }
        }
    }

    override fun getBatteryLevels(): Flow<BatteryLevels> {
        return cachedBatteryLevels.filterNotNull().map {
            BatteryLevels(it.mapNotNull { battery -> battery.toBatteryLevel() })
        }
    }

    override fun getBatteryLevel(name: String): BatteryLevels.BatteryLevel? {
        return runBlocking {
            getBatteryLevels().firstNotNull().levels.firstOrNull {
                it.name == name
            }
        }
    }

}