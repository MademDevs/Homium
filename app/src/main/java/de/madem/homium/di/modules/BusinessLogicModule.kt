package de.madem.homium.di.modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.madem.homium.databases.AppDatabase
import de.madem.homium.utilities.RecipeImporter

@Module
@InstallIn(SingletonComponent::class)
object BusinessLogicModule {

    @Provides
    fun provideRecipeImporter(db: AppDatabase) : RecipeImporter {
        return RecipeImporter(db)
    }
}