package com.kieronquinn.app.smartspacer.plugin.countdown.repositories

import com.kieronquinn.app.smartspacer.plugin.countdown.model.database.CountdownDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl

class DatabaseRepositoryImpl(
    database: CountdownDatabase
): DatabaseRepositoryImpl(_complicationData = database.complicationDataDao())