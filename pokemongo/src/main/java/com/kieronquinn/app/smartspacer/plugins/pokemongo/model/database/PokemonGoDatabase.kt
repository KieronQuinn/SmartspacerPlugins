package com.kieronquinn.app.smartspacer.plugins.pokemongo.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.ComplicationDataDao

@Database(entities = [
    ComplicationData::class
], version = 1, exportSchema = false)
abstract class PokemonGoDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): PokemonGoDatabase {
            return Room.databaseBuilder(
                context,
                PokemonGoDatabase::class.java,
                "pokemongo"
            ).build()
        }
    }

    abstract fun complicationDataDao(): ComplicationDataDao

}