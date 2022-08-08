package de.madem.homium.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import de.madem.homium.models.*


@Database(
    entities = [
        Product::class, ShoppingItem::class,
        InventoryItem::class, Recipe::class,
        RecipeIngredient::class, RecipeDescription::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppDatabaseTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingDao(): ShoppingDao
    abstract fun recipeDao(): RecipeDao
    abstract fun inventoryDao(): InventoryDao
}