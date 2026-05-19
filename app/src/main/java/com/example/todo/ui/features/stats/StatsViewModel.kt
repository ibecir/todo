package com.example.todo.ui.features.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.session.SessionManager
import com.example.todo.domain.model.ItemStats
import com.example.todo.domain.model.TodoStats
import com.example.todo.domain.repository.ItemRepository
import com.example.todo.domain.repository.StatsRepository
import com.example.todo.domain.repository.TodoRepository
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
    private val statsRepository: StatsRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            sessionManager.userId.flatMapLatest { userId ->
                if (userId != -1) todoRepository.getTodoStats(userId)
                else flowOf(TodoStats(0, 0, 0))
            }.collect { stats ->
                _uiState.update { it.copy(todoStats = stats) }
            }
        }
        viewModelScope.launch {
            sessionManager.userId.flatMapLatest { userId ->
                if (userId != -1) itemRepository.getItemStats(userId)
                else flowOf(ItemStats(0, 0))
            }.collect { stats ->
                _uiState.update { it.copy(itemStats = stats) }
            }
        }
    }

    fun onExportStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true, exportMessage = null) }
            
            val currentState = _uiState.value
            val result = statsRepository.exportStats(
                currentState.todoStats, 
                currentState.itemStats
            )

            result.onSuccess { uri ->
                _uiState.update { 
                    it.copy(
                        isExporting = false, 
                        exportMessage = "Stats exported to Downloads",
                        exportUri = uri
                    ) 
                }
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(
                        isExporting = false, 
                        exportMessage = "Export failed: ${e.message}",
                        exportUri = null
                    ) 
                }
            }
        }
    }

    fun clearExportMessage() {
        _uiState.update { it.copy(exportMessage = null, exportUri = null) }
    }
}
