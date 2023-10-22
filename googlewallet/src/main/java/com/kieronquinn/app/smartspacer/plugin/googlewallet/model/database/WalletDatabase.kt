package com.kieronquinn.app.smartspacer.plugin.googlewallet.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kieronquinn.app.smartspacer.plugin.googlewallet.utils.room.GsonSetConverter
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetData
import com.kieronquinn.app.smartspacer.plugin.shared.model.database.TargetDataDao
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValueConverter

@Database(entities = [
    TargetData::class,
    WalletValuable::class
], version = 1, exportSchema = false)
@TypeConverters(EncryptedValueConverter::class, GsonSetConverter::class)
abstract class WalletDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): WalletDatabase {
            return Room.databaseBuilder(
                context,
                WalletDatabase::class.java,
                "wallet"
            ).build()
        }
    }

    abstract fun walletValuableDao(): WalletValuableDao
    abstract fun targetDataDao(): TargetDataDao

}