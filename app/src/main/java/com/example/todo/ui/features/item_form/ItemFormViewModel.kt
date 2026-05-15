package com.example.todo.ui.features.item_form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.data.session.SessionManager
import com.example.todo.domain.model.Item
import com.example.todo.domain.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val itemRepository: ItemRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val todoId: Int = checkNotNull(savedStateHandle["todoId"])
    private val itemId: Int = savedStateHandle["itemId"] ?: -1
    val isEditing: Boolean = itemId > 0

    private val _uiState = MutableStateFlow<ItemFormUiState>(ItemFormUiState.Loading)
    val uiState: StateFlow<ItemFormUiState> = _uiState.asStateFlow()

    private val _navigationEvent = Channel<ItemFormNavigationEvent>(Channel.BUFFERED)
    val navigationEvent: Flow<ItemFormNavigationEvent> = _navigationEvent.receiveAsFlow()

    init {
        viewModelScope.launch {
            val existing = if (isEditing) itemRepository.getItemById(itemId, sessionManager.loggedInUserId) else null
            _uiState.value = ItemFormUiState.Ready(existing)
        }
    }

    fun onSaveClick(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _uiState.value = ItemFormUiState.Saving
            val now = System.currentTimeMillis()
            if (isEditing) {
                val existing = (_uiState.value as? ItemFormUiState.Ready)?.existingItem
                    ?: itemRepository.getItemById(itemId, sessionManager.loggedInUserId)
                    ?: return@launch
                itemRepository.update(
                    existing.copy(
                        name = name.trim(),
                        description = description.trim(),
                        updatedAt = now
                    )
                )
            } else {
                val newId = itemRepository.insert(
                    Item(
                        userId = sessionManager.loggedInUserId,
                        name = name.trim(),
                        description = description.trim(),
                        createdAt = now,
                        updatedAt = now
                    )
                )
                itemRepository.addItemToTodo(todoId, newId.toInt())
            }
            _navigationEvent.send(ItemFormNavigationEvent.NavigateBack)
        }
    }
}
