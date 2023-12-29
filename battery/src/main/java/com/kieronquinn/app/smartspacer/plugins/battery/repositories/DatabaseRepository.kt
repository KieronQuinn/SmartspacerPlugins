package com.kieronquinn.app.smartspacer.plugins.battery.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.battery.model.database.BatteryDatabase
import com.kieronquinn.app.smartspacer.plugins.battery.model.database.CachedBatteryLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

interface DatabaseRepository {

    fun getCachedBatteryLevels(): Flow<List<CachedBatteryLevel>>
    suspend fun setCachedBatteryLevel(level: CachedBatteryLevel)
    suspend fun setCachedBatteryLevelConnected(name: String, isConnected: Boolean)
    suspend fun deleteCachedBatteryLevel(name: String)

}

class DatabaseRepositoryImpl(
    database: BatteryDatabase
): DatabaseRepository, DatabaseRepositoryImpl(_complicationData = database.complicationDataDao()) {

    private val cachedBatteryLevelDao = database.cachedBatteryLevelDao()

    override fun getCachedBatteryLevels(): Flow<List<CachedBatteryLevel>> {
        return cachedBatteryLevelDao.getAll()
    }

    override suspend fun setCachedBatteryLevel(level: CachedBatteryLevel) {
        withContext(Dispatchers.IO) {
            cachedBatteryLevelDao.set(level)
        }
    }

    override suspend fun setCachedBatteryLevelConnected(name: String, isConnected: Boolean) {
        withContext(Dispatchers.IO) {
            cachedBatteryLevelDao.setConnected(name, isConnected)
        }
    }

    override suspend fun deleteCachedBatteryLevel(name: String) {
        withContext(Dispatchers.IO) {
            cachedBatteryLevelDao.delete(name)
        }
    }

}