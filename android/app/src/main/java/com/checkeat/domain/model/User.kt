package com.checkeat.domain.model

import java.util.Date

/**
 * Модель пользователя
 */
data class User(
    val id: Int,
    val email: String,
    val name: String,
    val createdAt: Date
)

/**
 * Данные для авторизации
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val user: User
)
