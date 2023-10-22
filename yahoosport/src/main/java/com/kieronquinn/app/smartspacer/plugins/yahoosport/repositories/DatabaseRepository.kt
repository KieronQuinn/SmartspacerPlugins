package com.kieronquinn.app.smartspacer.plugins.yahoosport.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.yahoosport.model.database.YahooSportDatabase

class DatabaseRepositoryImpl(
    database: YahooSportDatabase
): DatabaseRepositoryImpl(database.targetDataDao())