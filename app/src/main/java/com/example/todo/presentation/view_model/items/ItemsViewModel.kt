package com.example.todo.presentation.view_model.items

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.model.local.entity.ItemEntity
import com.example.todo.model.repository.ItemRepository
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

import android.util.Log

@HiltViewModel
class ItemsViewModel @Inject constructor(
    private val itemRepository: ItemRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ItemsUiState())
    val uiState: StateFlow<ItemsUiState> = _uiState.asStateFlow()

    init {
        Log.d("ItemsViewModel", "Init called")
        viewModelScope.launch {
            sessionManager.userId.flatMapLatest { userId ->
                if (userId != -1) itemRepository.getAllItems(userId) else flowOf(emptyList())
            }.collect { items ->
                Log.d("ItemsViewModel", "Collected items: ${items.size}")
                _uiState.update { it.copy(items = items) }
            }
        }
    }

    fun onAddClick() {
        _uiState.update { it.copy(isDialogOpen = true, editingItem = null) }
    }

    fun onEditClick(item: ItemEntity) {
        _uiState.update { it.copy(isDialogOpen = true, editingItem = item) }
    }

    fun onDismissDialog() {
        _uiState.update { it.copy(isDialogOpen = false, editingItem = null) }
    }

    fun onSave(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val editing = _uiState.value.editingItem
            if (editing != null) {
                itemRepository.update(
                    editing.copy(name = name.trim(), description = description.trim(), updatedAt = now)
                )
            } else {
                itemRepository.insert(
                    ItemEntity(
                        userId = sessionManager.loggedInUserId,
                        name = name.trim(),
                        description = description.trim(),
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
        }
        _uiState.update { it.copy(isDialogOpen = false, editingItem = null) }
    }

    fun onDelete(item: ItemEntity) {
        viewModelScope.launch { itemRepository.delete(item) }
    }
}
