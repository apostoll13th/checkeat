package com.checkeat.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkeat.data.local.entity.NoteEntity
import com.checkeat.data.repository.NoteRepository
import com.checkeat.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана заметок
 */
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: NoteRepository
) : ViewModel() {

    private val _notesState = MutableStateFlow<UiState<List<NoteEntity>>>(UiState.Loading)
    val notesState: StateFlow<UiState<List<NoteEntity>>> = _notesState.asStateFlow()

    private val _createNoteState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val createNoteState: StateFlow<UiState<Unit>> = _createNoteState.asStateFlow()

    init {
        loadNotes()
    }

    /**
     * Загрузить заметки
     */
    private fun loadNotes() {
        viewModelScope.launch {
            try {
                repository.getAllNotesFlow().collect { notes ->
                    _notesState.value = UiState.Success(notes)
                }
            } catch (e: Exception) {
                _notesState.value = UiState.Error(
                    e.message ?: "Ошибка загрузки заметок"
                )
            }
        }
    }

    /**
     * Создать заметку
     */
    fun createNote(title: String, content: String, tags: List<String> = emptyList()) {
        viewModelScope.launch {
            _createNoteState.value = UiState.Loading
            try {
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
            } catch (e: Exception) {
                _createNoteState.value = UiState.Error(
                    e.message ?: "Неизвестная ошибка"
                )
            }
        }
    }

    /**
     * Удалить заметку
     */
    fun deleteNote(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteNote(id)
            } catch (e: Exception) {
                _notesState.value = UiState.Error(
                    e.message ?: "Ошибка удаления заметки"
                )
            }
        }
    }

    /**
     * Поиск заметок
     */
    fun searchNotes(query: String) {
        viewModelScope.launch {
            try {
                repository.searchNotes(query).collect { notes ->
                    _notesState.value = UiState.Success(notes)
                }
            } catch (e: Exception) {
                _notesState.value = UiState.Error(
                    e.message ?: "Ошибка поиска"
                )
            }
        }
    }

    /**
     * Сбросить состояние создания
     */
    fun resetCreateState() {
        _createNoteState.value = UiState.Idle
    }
}
