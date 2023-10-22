package com.kieronquinn.app.smartspacer.plugins.pokemongo.repositories

import com.kieronquinn.app.smartspacer.plugin.shared.repositories.DatabaseRepositoryImpl
import com.kieronquinn.app.smartspacer.plugins.pokemongo.model.database.PokemonGoDatabase

class DatabaseRepository(
    database: PokemonGoDatabase
): DatabaseRepositoryImpl(
    _complicationData = database.complicationDataDao()
)