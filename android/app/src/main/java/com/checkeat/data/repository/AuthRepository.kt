package com.checkeat.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.checkeat.data.remote.api.CheckEatApi
import com.checkeat.data.remote.dto.AuthResponse
import com.checkeat.data.remote.dto.LoginRequest
import com.checkeat.data.remote.dto.RegisterRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для аутентификации
 */
@Singleton
class AuthRepository @Inject constructor(
    private val api: CheckEatApi,
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }

    /**
     * Проверить авторизацию
     */
    val isAuthenticated: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY] != null
    }

    /**
     * Получить токен
     */
    val accessToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }

    /**
     * Регистрация
     */
    suspend fun register(email: String, password: String, name: String): Result<AuthResponse> {
        return try {
            val request = RegisterRequest(email, password, name)
            val response = api.register(request)
            saveAuthData(response)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Вход
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val request = LoginRequest(email, password)
            val response = api.login(request)
            saveAuthData(response)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Выход
     */
    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Сохранить данные авторизации
     */
    private suspend fun saveAuthData(response: AuthResponse) {
        dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = response.accessToken
            preferences[REFRESH_TOKEN_KEY] = response.refreshToken
            preferences[USER_EMAIL_KEY] = response.user.email
            preferences[USER_NAME_KEY] = response.user.name
        }
    }

    /**
     * Получить имя пользователя
     */
    val userName: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: "Пользователь"
    }

    /**
     * Получить email
     */
    val userEmail: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY] ?: ""
    }
}
