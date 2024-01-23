package com.kieronquinn.app.smartspacer.plugin.amazon.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValueConverter

@Database(entities = [
    AmazonDelivery::class
], version = 1, exportSchema = false)
@TypeConverters(EncryptedValueConverter::class)
abstract class AmazonDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): AmazonDatabase {
            return Room.databaseBuilder(
                context,
                AmazonDatabase::class.java,
                "amazon_orders"
            ).build()
        }
    }

    abstract fun amazonDeliveryDao(): AmazonDeliveryDao

}