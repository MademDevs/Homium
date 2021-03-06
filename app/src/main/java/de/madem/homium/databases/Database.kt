package de.madem.homium.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import de.madem.homium.application.HomiumApplication
import de.madem.homium.models.*


@Database(
    entities = [Product::class, ShoppingItem::class, InventoryItem::class, Recipe::class, RecipeIngredient::class, RecipeDescription::class],
    version = 1, exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    //userDao for accessing database content
    abstract fun itemDao(): ItemDao

    abstract fun recipeDao(): RecipeDao
    abstract fun inventoryDao(): InventoryDao

    //Singleton
    companion object{
        private lateinit var instance: AppDatabase

        fun getInstance(context: Context = HomiumApplication.appContext!!): AppDatabase{
            if (!::instance.isInitialized) {
                instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "database")
                    .build()
            }

            return instance
        }

    }
}