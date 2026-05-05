package com.example.todo.presentation.view_model.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.model.dto.ItemStatsDto
import com.example.todo.model.dto.TodoStatsDto
import com.example.todo.model.repository.ItemRepository
import com.example.todo.model.repository.TodoRepository
import com.example.todo.model.session.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val itemRepository: ItemRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.userId.flatMapLatest { userId ->
                if (userId != -1) todoRepository.getTodoStats(userId)
                else flowOf(TodoStatsDto(0, 0, 0))
            }.collect { stats ->
                _uiState.update { it.copy(todoStats = stats) }
            }
        }
        viewModelScope.launch {
            sessionManager.userId.flatMapLatest { userId ->
                if (userId != -1) itemRepository.getItemStats(userId)
                else flowOf(ItemStatsDto(0, 0))
            }.collect { stats ->
                _uiState.update { it.copy(itemStats = stats) }
            }
        }
    }
}
