package com.checkeat.domain.model

import java.util.Date

/**
 * Модель заметки
 */
data class Note(
    val id: Int,
    val userId: Int,
    val analysisId: Int?,
    val title: String,
    val content: String,
    val tags: List<String>,
    val createdAt: Date,
    val updatedAt: Date
)
