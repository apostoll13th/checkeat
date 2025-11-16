package com.checkeat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.checkeat.data.local.converter.Converters
import com.checkeat.data.local.dao.FoodAnalysisDao
import com.checkeat.data.local.dao.NoteDao
import com.checkeat.data.local.entity.FoodAnalysisEntity
import com.checkeat.data.local.entity.NoteEntity

/**
 * Главная база данных приложения
 */
@Database(
    entities = [
        FoodAnalysisEntity::class,
        NoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodAnalysisDao(): FoodAnalysisDao
    abstract fun noteDao(): NoteDao

    companion object {
        const val DATABASE_NAME = "checkeat_db"
    }
}
