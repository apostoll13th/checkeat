package com.checkeat.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.checkeat.data.local.converter.Converters
import java.util.Date

@Entity(tableName = "notes")
@TypeConverters(Converters::class)
data class NoteEntity(
    @PrimaryKey
    val id: Int,
    val userId: Int,
    val analysisId: Int?,
    val title: String,
    val content: String,
    val tags: List<String>,
    val createdAt: Date,
    val updatedAt: Date,
    val synced: Boolean = true
)
