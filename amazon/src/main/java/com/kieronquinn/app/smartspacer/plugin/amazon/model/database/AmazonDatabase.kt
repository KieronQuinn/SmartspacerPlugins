package com.kieronquinn.app.smartspacer.plugin.amazon.model.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kieronquinn.app.smartspacer.plugin.amazon.utils.extensions.clearEncryptedBitmaps
import com.kieronquinn.app.smartspacer.plugin.shared.utils.room.EncryptedValueConverter

@Database(entities = [
    AmazonDelivery::class
], version = 2, exportSchema = false)
@TypeConverters(EncryptedValueConverter::class)
abstract class AmazonDatabase: RoomDatabase() {

    companion object {
        fun getDatabase(context: Context): AmazonDatabase {
            return Room.databaseBuilder(
                context,
                AmazonDatabase::class.java,
                "amazon_orders"
            ).addMigrations(MIGRATION_1_2(context)).build()
        }

        private fun MIGRATION_1_2(context: Context): Migration {
            return object: Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("DROP TABLE IF EXISTS \"AmazonDelivery\";")
                    db.execSQL("CREATE TABLE IF NOT EXISTS \"AmazonDelivery\" (\n" +
                            "\t\"id\"\tTEXT NOT NULL,\n" +
                            "\t\"order_id\"\tTEXT NOT NULL,\n" +
                            "\t\"shipment_id\"\tTEXT,\n" +
                            "\t\"index\"\tINTEGER NOT NULL,\n" +
                            "\t\"name\"\tBLOB NOT NULL,\n" +
                            "\t\"image_url\"\tBLOB NOT NULL,\n" +
                            "\t\"order_details_url\"\tBLOB,\n" +
                            "\t\"status\"\tBLOB NOT NULL,\n" +
                            "\t\"message\"\tBLOB NOT NULL,\n" +
                            "\t\"tracking_id\"\tBLOB,\n" +
                            "\t\"customer_id\"\tBLOB,\n" +
                            "\t\"csrf_token\"\tBLOB,\n" +
                            "\t\"tracking_data\"\tBLOB,\n" +
                            "\t\"tracking_status\"\tBLOB,\n" +
                            "\t\"dismissed_at_status\"\tTEXT,\n" +
                            "\tPRIMARY KEY(\"id\")\n" +
                            ");")
                    //Bitmaps will now have the wrong name. Clear them.
                    context.clearEncryptedBitmaps()
                }
            }
        }
    }

    abstract fun amazonDeliveryDao(): AmazonDeliveryDao

}