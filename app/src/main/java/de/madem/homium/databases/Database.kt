package de.madem.homium.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.madem.homium.application.HomiumApplication
import de.madem.homium.models.*


@Database(
    entities = [Product::class, ShoppingItem::class, InventoryItem::class, Recipe::class, Ingredients::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    //userDao for accessing database content
    abstract fun itemDao(): ItemDao
    abstract fun recipeDao(): RecipeDao

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