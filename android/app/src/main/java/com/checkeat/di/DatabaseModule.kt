package com.checkeat.di

import android.content.Context
import androidx.room.Room
import com.checkeat.data.local.AppDatabase
import com.checkeat.data.local.dao.FoodAnalysisDao
import com.checkeat.data.local.dao.NoteDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt модуль для предоставления зависимостей базы данных
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFoodAnalysisDao(database: AppDatabase): FoodAnalysisDao {
        return database.foodAnalysisDao()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }
}
