package de.madem.homium.di.modules

import androidx.annotation.Keep
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.RoomInventoryRepository
import de.madem.homium.databases.RoomProductRepository
import de.madem.homium.databases.RoomShoppingRepository
import de.madem.homium.repositories.InventoryRepository
import de.madem.homium.repositories.ProductRepository
import de.madem.homium.repositories.ShoppingRepository
import javax.inject.Singleton

@Keep
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Keep
    @Singleton
    @Provides
    fun provideInventoryRepository(database: AppDatabase): InventoryRepository {
        return RoomInventoryRepository(database.inventoryDao())
    }

    @Keep
    @Singleton
    @Provides
    fun provideShoppingRepository(database: AppDatabase): ShoppingRepository {
        return RoomShoppingRepository(database.shoppingDao())
    }

    @Keep
    @Singleton
    @Provides
    fun provideProductRepository(database: AppDatabase): ProductRepository {
        return RoomProductRepository(database.productDao())
    }

}