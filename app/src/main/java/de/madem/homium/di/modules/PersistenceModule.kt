package de.madem.homium.di.modules

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.madem.homium.databases.AppDatabase
import de.madem.homium.databases.AppDatabaseMigrations
import javax.inject.Singleton

@Keep
@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {

    @Keep
    @Singleton
    @Provides
    fun provideAppDatabaseMigrations(@ApplicationContext context: Context) : AppDatabaseMigrations {
        return AppDatabaseMigrations(context)
    }

    @Keep
    @Singleton
    @Provides
    fun provideAppDatabase(
        @ApplicationContext context: Context,
        migrations: AppDatabaseMigrations
    ) : AppDatabase {
        return Room
            .databaseBuilder(context, AppDatabase::class.java, "database")
            .addMigrations(*migrations.all())
            .build()
    }
}