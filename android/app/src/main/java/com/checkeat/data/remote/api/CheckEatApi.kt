package com.checkeat.data.remote.api

import com.checkeat.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.http.*

/**
 * CheckEat API интерфейс
 */
interface CheckEatApi {

    // Аутентификация
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("auth/profile")
    suspend fun getProfile(): UserDto

    // Анализ еды
    @Multipart
    @POST("analyze")
    suspend fun analyzeFood(
        @Part file: MultipartBody.Part
    ): FoodAnalysisDto

    @GET("analyze/history")
    suspend fun getHistory(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20
    ): FoodAnalysisListResponse

    @GET("analyze/history/{id}")
    suspend fun getAnalysis(@Path("id") id: Int): FoodAnalysisDto

    @DELETE("analyze/history/{id}")
    suspend fun deleteAnalysis(@Path("id") id: Int)

    // Заметки
    @POST("notes")
    suspend fun createNote(@Body request: NoteRequest): NoteDto

    @GET("notes")
    suspend fun getNotes(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("search") search: String? = null
    ): NoteListResponse

    @GET("notes/{id}")
    suspend fun getNote(@Path("id") id: Int): NoteDto

    @PUT("notes/{id}")
    suspend fun updateNote(@Path("id") id: Int, @Body request: NoteUpdateRequest): NoteDto

    @DELETE("notes/{id}")
    suspend fun deleteNote(@Path("id") id: Int)

    // Статистика
    @GET("stats/daily")
    suspend fun getDailyStats(@Query("date_param") date: String? = null): DailyStatsDto

    @GET("stats/weekly")
    suspend fun getWeeklyStats(): StatsRangeDto

    @GET("stats/monthly")
    suspend fun getMonthlyStats(): StatsRangeDto

    @GET("stats/chart")
    suspend fun getChartData(@Query("days") days: Int = 7): ChartDataDto

    // Экспорт
    @GET("export/csv/history")
    suspend fun exportHistoryCsv(
        @Query("start_date") startDate: String? = null,
        @Query("end_date") endDate: String? = null
    ): String

    @GET("export/csv/stats")
    suspend fun exportStatsCsv(@Query("days") days: Int = 30): String

    @GET("export/csv/notes")
    suspend fun exportNotesCsv(): String
}

// Дополнительные DTO
data class NoteRequest(
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val analysis_id: Int? = null
)

data class NoteUpdateRequest(
    val title: String? = null,
    val content: String? = null,
    val tags: List<String>? = null
)

data class NoteDto(
    val id: Int,
    val user_id: Int,
    val analysis_id: Int?,
    val title: String,
    val content: String,
    val tags: List<String>,
    val created_at: String,
    val updated_at: String
)

data class NoteListResponse(
    val items: List<NoteDto>,
    val total: Int,
    val page: Int,
    val page_size: Int,
    val pages: Int
)

data class DailyStatsDto(
    val date: String,
    val total_calories: Float,
    val total_proteins: Float,
    val total_fats: Float,
    val total_carbs: Float,
    val meals_count: Int
)

data class StatsRangeDto(
    val daily_stats: List<DailyStatsDto>,
    val period_total: DailyStatsDto,
    val average_per_day: DailyStatsDto
)

data class ChartDataDto(
    val calorie_data: List<CalorieDataPointDto>,
    val macro_data: List<MacroDataPointDto>
)

data class CalorieDataPointDto(
    val date: String,
    val calories: Float
)

data class MacroDataPointDto(
    val date: String,
    val proteins: Float,
    val fats: Float,
    val carbs: Float
)
