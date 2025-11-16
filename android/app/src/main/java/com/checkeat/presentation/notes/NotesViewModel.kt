package com.checkeat.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.local.entity.NoteEntity
import com.checkeat.data.repository.NoteRepository
import com.checkeat.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана заметок с улучшенным управлением памятью
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Улучшение: использование stateIn вместо бесконечного collect для предотвращения утечек памяти
    val notesState: StateFlow<UiState<List<NoteEntity>>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllNotesFlow()
            } else {
                repository.searchNotes(query)
            }
        }
        .map<List<NoteEntity>, UiState<List<NoteEntity>>> { notes ->
            UiState.Success(notes)
        }
        .catch { e ->
            emit(UiState.Error(e.message ?: "Ошибка загрузки заметок"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UiState.Loading
        )

    private val _createNoteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createNoteState: StateFlow<UiState<Unit>> = _createNoteState.asStateFlow()

    /**
     * Создать заметку
     */
    fun createNote(title: String, content: String, tags: List<String> = emptyList()) {
        viewModelScope.launch {
            _createNoteState.value = UiState.Loading

            val result = repository.createNote(title, content, tags)
            result.fold(
                onSuccess = {
                    _createNoteState.value = UiState.Success(Unit)
                },
                onFailure = { error ->
                    _createNoteState.value = UiState.Error(
                        error.message ?: "Ошибка создания заметки"
                    )
                }
            )
        }
    }

    /**
     * Удалить заметку
     */
    fun deleteNote(id: Int) {
        viewModelScope.launch {
            repository.deleteNote(id)
            // Список обновится автоматически через Flow
        }
    }

    /**
     * Обновить поисковый запрос
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Очистить поиск
     */
    fun clearSearch() {
        _searchQuery.value = ""
    }

    /**
     * Сбросить состояние создания
     */
    fun resetCreateState() {
        _createNoteState.value = UiState.Idle
    }
}
