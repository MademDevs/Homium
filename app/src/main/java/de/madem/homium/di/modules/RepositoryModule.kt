package de.madem.homium.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.RoomInventoryRepository
import de.madem.homium.repositories.InventoryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideInventoryRepository(database: AppDatabase): InventoryRepository {
        return RoomInventoryRepository(database.inventoryDao())
    }


}