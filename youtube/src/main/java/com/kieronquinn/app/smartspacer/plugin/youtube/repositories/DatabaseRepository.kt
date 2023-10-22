package com.kieronquinn.app.smartspacer.plugin.youtube.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.youtube.model.database.YouTubeDatabase

class DatabaseRepositoryImpl(
    database: YouTubeDatabase
): DatabaseRepositoryImpl(_complicationData = database.complicationDataDao())