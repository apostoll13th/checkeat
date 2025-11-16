package com.checkeat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.checkeat.data.local.converter.Converters
import com.checkeat.data.local.dao.*
import com.checkeat.data.local.entity.*

/**
 * Главная база данных приложения
 */
@Database(
    entities = [
        FoodAnalysisEntity::class,
        NoteEntity::class,
        WaterIntakeEntity::class,
        DailyGoalEntity::class,
        FavoriteEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodAnalysisDao(): FoodAnalysisDao
    abstract fun noteDao(): NoteDao
    abstract fun waterIntakeDao(): WaterIntakeDao
    abstract fun dailyGoalDao(): DailyGoalDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        const val DATABASE_NAME = "checkeat_db"
    }
}
