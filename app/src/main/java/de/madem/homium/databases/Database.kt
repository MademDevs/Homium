package de.madem.homium.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.madem.homium.application.HomiumApplication
import de.madem.homium.models.InventoryItem
import de.madem.homium.models.Product
import de.madem.homium.models.ShoppingItem


@Database(
    entities = [Product::class, ShoppingItem::class, InventoryItem::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    //userDao for accessing database content
    abstract fun itemDao(): ItemDao

    //Singleton
    companion object{
        private var INSTANCE: AppDatabase? = null
        fun getInstance(): AppDatabase{
            if (INSTANCE == null){
                INSTANCE = Room.databaseBuilder(
                    HomiumApplication.appContext!!,
                    AppDatabase::class.java,
                    "database")
                    .build()
            }

            return INSTANCE as AppDatabase
        }
    }
}