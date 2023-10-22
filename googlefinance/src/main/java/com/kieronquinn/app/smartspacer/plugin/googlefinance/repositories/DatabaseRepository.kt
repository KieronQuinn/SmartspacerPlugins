package com.kieronquinn.app.smartspacer.plugin.googlefinance.repositories

import com.kieronquinn.app.smartspacer.plugin.googlefinance.model.database.GoogleFinanceDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl

class DatabaseRepositoryImpl(
    database: GoogleFinanceDatabase
): DatabaseRepositoryImpl(database.targetDataDao())