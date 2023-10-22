package com.kieronquinn.app.smartspacer.plugins.battery.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.battery.model.database.BatteryDatabase

class DatabaseRepositoryImpl(
    database: BatteryDatabase
): DatabaseRepositoryImpl(_complicationData = database.complicationDataDao())