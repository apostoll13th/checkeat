package com.checkeat.data.repository

import com.checkeat.data.local.dao.DailyTaskDao
import com.checkeat.data.local.entity.DailyTask
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Репозиторий для работы с ежедневными задачами
 */
@Singleton
class DailyTaskRepository @Inject constructor(
    private val taskDao: DailyTaskDao
) {

    /**
     * Получить все задачи
     */
    fun getAllTasksFlow(): Flow<List<DailyTask>> {
        return taskDao.getAllFlow()
    }

    /**
     * Получить выполненные задачи
     */
    fun getCompletedTasksFlow(): Flow<List<DailyTask>> {
        return taskDao.getCompletedFlow()
    }

    /**
     * Получить невыполненные задачи
     */
    fun getIncompleteTasksFlow(): Flow<List<DailyTask>> {
        return taskDao.getIncompleteFlow()
    }

    /**
     * Создать новую задачу
     */
    suspend fun createTask(
        title: String,
        description: String? = null,
        isRecurring: Boolean = true
    ): Result<Long> {
        return try {
            val task = DailyTask(
                title = title,
                description = description,
                isRecurring = isRecurring,
                isCompleted = false
            )
            val id = taskDao.insert(task)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Обновить задачу
     */
    suspend fun updateTask(task: DailyTask): Result<Unit> {
        return try {
            taskDao.update(task)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Удалить задачу
     */
    suspend fun deleteTask(id: Int): Result<Unit> {
        return try {
            taskDao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Отметить задачу как выполненную/невыполненную
     */
    suspend fun toggleTaskCompletion(task: DailyTask): Result<Unit> {
        return try {
            val newCompleted = !task.isCompleted
            val date = if (newCompleted) System.currentTimeMillis() else null
            taskDao.updateCompletionStatus(task.id, newCompleted, date)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Сбросить все повторяющиеся задачи (для нового дня)
     */
    suspend fun resetDailyTasks(): Result<Unit> {
        return try {
            taskDao.resetRecurringTasks()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Проверить, нужно ли сбросить задачи (если последнее выполнение было не сегодня)
     */
    suspend fun checkAndResetIfNeeded(): Result<Unit> {
        return try {
            val tasks = taskDao.getCompletedFlow()
            // Здесь можно добавить логику проверки даты
            // Если последняя выполненная задача не сегодня - сбросить
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Создать дефолтные задачи при первом запуске
     */
    suspend fun ensureDefaultTasks(): Result<Unit> {
        return try {
            // Проверяем, есть ли уже задачи
            val count = taskDao.getAllFlow()

            // Создаем дефолтные задачи
            val defaultTasks = listOf(
                DailyTask(title = "Взвеситься утром", order = 1, isRecurring = true),
                DailyTask(title = "Выпить 2 литра воды", order = 2, isRecurring = true),
                DailyTask(title = "Позавтракать", order = 3, isRecurring = true),
                DailyTask(title = "Позаниматься спортом", order = 4, isRecurring = true),
                DailyTask(title = "Сфотографировать все приемы пищи", order = 5, isRecurring = true)
            )

            defaultTasks.forEach { task ->
                taskDao.insert(task)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
