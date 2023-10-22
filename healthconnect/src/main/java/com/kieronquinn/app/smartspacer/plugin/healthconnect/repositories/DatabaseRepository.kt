package com.kieronquinn.app.smartspacer.plugin.healthconnect.repositories

import android.content.Context
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.database.HealthConnectDatabase
import com.kieronquinn.app.smartspacer.plugin.healthconnect.model.database.HealthData
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.shared.utils.extensions.firstNotNull
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerComplicationProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

abstract class DatabaseRepository(
    database: HealthConnectDatabase
): DatabaseRepositoryImpl(
    _complicationData = database.complicationDataDao()
) {

    abstract fun getAllHealthData(): Flow<List<HealthData>>
    abstract fun getHealthData(smartspacerId: String): HealthData?
    abstract fun getHealthDataAsFlow(smartspacerId: String): Flow<HealthData?>
    abstract suspend fun setHealthData(healthData: HealthData)
    abstract suspend fun removeHealthData(smartspacerId: String)

}

class DatabaseRepositoryImpl(
    private val context: Context,
    database: HealthConnectDatabase
): DatabaseRepository(database) {

    private val scope = MainScope()
    private val healthDataDao = database.healthDataDao()

    private val healthData = healthDataDao.getHealthData()
        .flowOn(Dispatchers.IO)
        .stateIn(scope, SharingStarted.Eagerly, null)

    override fun getAllHealthData() = healthData.filterNotNull()

    override fun getHealthData(smartspacerId: String): HealthData? {
        return runBlocking {
            healthData.firstNotNull().firstOrNull {
                it.smartspacerId == smartspacerId
            }
        }
    }

    override fun getHealthDataAsFlow(smartspacerId: String): Flow<HealthData?> {
        return healthData.filterNotNull().map {
            it.firstOrNull { data -> data.smartspacerId == smartspacerId }
        }
    }

    override suspend fun setHealthData(healthData: HealthData) {
        withContext(Dispatchers.IO){
            healthDataDao.setHealthData(healthData)
        }
    }

    override suspend fun removeHealthData(smartspacerId: String) {
        withContext(Dispatchers.IO) {
            healthDataDao.deleteHealthData(smartspacerId)
        }
    }

    private fun setupChanges() = scope.launch {
        healthData.filterNotNull().collect {
            it.forEach { data -> notifyChange(data) }
        }
    }

    private fun notifyChange(healthData: HealthData) {
        SmartspacerComplicationProvider.notifyChange(
            context, healthData.authority, healthData.smartspacerId
        )
    }

    init {
        setupChanges()
    }

}