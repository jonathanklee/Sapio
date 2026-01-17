package com.klee.sapio.data.local

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sapio.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideEvaluationDao(database: AppDatabase): EvaluationDao = database.evaluationDao()

    @Provides
    fun provideIconDao(database: AppDatabase): IconDao = database.iconDao()
}
