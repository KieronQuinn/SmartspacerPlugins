package com.kieronquinn.app.smartspacer.plugin.googlehome.repositories

import com.kieronquinn.app.smartspacer.plugin.googlehome.model.database.GoogleHomeDatabase
import com.kieronquinn.app.smartspacer.plugin.googlehome.model.database.WidgetItem
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class DatabaseRepository(
    database: GoogleHomeDatabase
): DatabaseRepositoryImpl(
    _complicationData = database.complicationDataDao(),
    _targetData = database.targetDataDao()
) {

    private val widgetItemDao = database.widgetItemDao()

    val items = widgetItemDao.getAll()
        .flowOn(Dispatchers.IO)

    suspend fun addItem(item: WidgetItem) {
        withContext(Dispatchers.IO) {
            widgetItemDao.insert(item)
        }
    }

    suspend fun deleteItem(smartspacerId: String) {
        withContext(Dispatchers.IO) {
            widgetItemDao.delete(smartspacerId)
        }
    }

}