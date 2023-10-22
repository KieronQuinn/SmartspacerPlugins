package com.kieronquinn.app.smartspacer.plugins.datausage.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.datausage.model.database.DataUsageDatabase

class DatabaseRepositoryImpl(
    database: DataUsageDatabase
): DatabaseRepositoryImpl(_complicationData = database.complicationDataDao())