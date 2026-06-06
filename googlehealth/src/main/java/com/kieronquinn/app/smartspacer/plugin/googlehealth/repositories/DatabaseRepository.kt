package com.kieronquinn.app.smartspacer.plugin.googlehealth.repositories

import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.database.GoogleHealthDatabase
import com.kieronquinn.app.smartspacer.plugin.googlehealth.model.database.HealthFocus
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DatabaseRepository(
    database: GoogleHealthDatabase
): DatabaseRepositoryImpl(
    _complicationData = database.complicationDataDao()
) {

    private val healthFocusDao = database.healthFocusDao()

    val healthItemCachedItems = healthFocusDao.getAll()
        .flowOn(Dispatchers.IO)
        .debounce(250L)

    suspend fun setHealthFocusItems(items: List<HealthFocus>) {
        withContext(Dispatchers.IO) {
            healthFocusDao.clear()
            healthFocusDao.insert(items)
        }
    }

}