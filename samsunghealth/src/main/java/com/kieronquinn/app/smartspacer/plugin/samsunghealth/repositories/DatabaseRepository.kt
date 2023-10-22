package com.kieronquinn.app.smartspacer.plugin.samsunghealth.repositories

import com.kieronquinn.app.smartspacer.plugin.samsunghealth.model.database.SamsungHealthDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl

class DatabaseRepository(
    database: SamsungHealthDatabase
): DatabaseRepositoryImpl(
    _complicationData = database.complicationDataDao()
)