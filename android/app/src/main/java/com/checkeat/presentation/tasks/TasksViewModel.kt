package com.checkeat.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.local.entity.DailyTask
import com.checkeat.data.repository.DailyTaskRepository
import com.checkeat.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для управления ежедневными задачами
 */
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: DailyTaskRepository
) : ViewModel() {

    init {
        // Создаем дефолтные задачи при первом запуске
        viewModelScope.launch {
            taskRepository.ensureDefaultTasks()
        }
    }

    // Все задачи
    val tasksState: StateFlow<UiState<List<DailyTask>>> = taskRepository
        .getAllTasksFlow()
        .map<List<DailyTask>, UiState<List<DailyTask>>> { tasks ->
            UiState.Success(tasks)
        }
        .catch { e ->
            emit(UiState.Error(e.message ?: "Ошибка загрузки задач"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    // Прогресс выполнения (процент выполненных задач)
    val completionProgress: StateFlow<Float> = tasksState.map { state ->
        when (state) {
            is UiState.Success -> {
                val tasks = state.data
                if (tasks.isEmpty()) 0f
                else {
                    val completed = tasks.count { it.isCompleted }
                    (completed.toFloat() / tasks.size.toFloat() * 100f).coerceIn(0f, 100f)
                }
            }
            else -> 0f
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0f
    )

    private val _createTaskState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val createTaskState: StateFlow<UiState<Long>> = _createTaskState.asStateFlow()

    /**
     * Переключить статус выполнения задачи
     */
    fun toggleTask(task: DailyTask) {
        viewModelScope.launch {
            taskRepository.toggleTaskCompletion(task)
        }
    }

    /**
     * Создать новую задачу
     */
    fun createTask(title: String, description: String? = null, isRecurring: Boolean = true) {
        viewModelScope.launch {
            _createTaskState.value = UiState.Loading

            val result = taskRepository.createTask(title, description, isRecurring)
            result.fold(
                onSuccess = { id ->
                    _createTaskState.value = UiState.Success(id)
                },
                onFailure = { error ->
                    _createTaskState.value = UiState.Error(
                        error.message ?: "Ошибка создания задачи"
                    )
                }
            )
        }
    }

    /**
     * Удалить задачу
     */
    fun deleteTask(id: Int) {
        viewModelScope.launch {
            taskRepository.deleteTask(id)
        }
    }

    /**
     * Сбросить все повторяющиеся задачи
     */
    fun resetDailyTasks() {
        viewModelScope.launch {
            taskRepository.resetDailyTasks()
        }
    }

    /**
     * Сбросить состояние создания
     */
    fun resetCreateState() {
        _createTaskState.value = UiState.Idle
    }
}
