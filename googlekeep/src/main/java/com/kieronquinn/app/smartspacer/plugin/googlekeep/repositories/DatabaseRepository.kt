package com.kieronquinn.app.smartspacer.plugin.googlekeep.repositories

import com.kieronquinn.app.smartspacer.plugin.googlekeep.model.database.GoogleKeepDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl

class DatabaseRepositoryImpl(
    database: GoogleKeepDatabase
): DatabaseRepositoryImpl(_targetData = database.targetDataDao())