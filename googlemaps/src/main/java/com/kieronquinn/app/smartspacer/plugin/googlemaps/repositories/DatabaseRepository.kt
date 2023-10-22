package com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories

import com.kieronquinn.app.smartspacer.plugin.googlemaps.model.database.GoogleMapsDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl

class DatabaseRepositoryImpl(
    database: GoogleMapsDatabase
): DatabaseRepositoryImpl(_targetData = database.targetDataDao())