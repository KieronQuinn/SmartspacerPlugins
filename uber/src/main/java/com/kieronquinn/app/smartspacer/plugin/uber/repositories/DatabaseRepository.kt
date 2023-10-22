package com.kieronquinn.app.smartspacer.plugin.uber.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.uber.model.database.UberDatabase

class DatabaseRepositoryImpl(
    database: UberDatabase
): DatabaseRepositoryImpl(database.targetDataDao())