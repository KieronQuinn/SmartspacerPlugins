package com.kieronquinn.app.smartspacer.plugin.controls.repositories

import com.kieronquinn.app.smartspacer.plugin.controls.model.database.ControlsDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl

class DatabaseRepositoryImpl(
    database: ControlsDatabase
): DatabaseRepositoryImpl(
    database.targetDataDao(),
    database.complicationDataDao(),
    database.requirementDataDao()
)