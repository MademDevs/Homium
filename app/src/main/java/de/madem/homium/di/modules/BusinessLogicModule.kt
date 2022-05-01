package de.madem.homium.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.madem.homium.databases.AppDatabase
import de.madem.homium.speech.SpeechAssistant
import de.madem.homium.ui.fragments.shopping.ShoppingToInventoryHandler
import de.madem.homium.utilities.CookingAssistant
import de.madem.homium.utilities.RecipeImporter
import java.lang.ref.WeakReference
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BusinessLogicModule {
    @Singleton
    @Provides
    fun provideSpeechAssistant(@ApplicationContext context: Context, db: AppDatabase): SpeechAssistant {
        return SpeechAssistant(context, db)
    }

    @Singleton
    @Provides
    fun provideCookingAssistant(@ApplicationContext context: Context, db: AppDatabase): CookingAssistant {
        return CookingAssistant(WeakReference(context), db)
    }

    @Singleton
    @Provides
    fun provideShoppingToInventoryHandler(@ApplicationContext context: Context, db: AppDatabase) : ShoppingToInventoryHandler {
        return ShoppingToInventoryHandler(context, db)
    }

    @Provides
    fun provideRecipeImporter(db: AppDatabase) : RecipeImporter {
        return RecipeImporter(db)
    }
}