package com.example.todo.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todo.model.dto.TagDto
import com.example.todo.model.local.entity.ItemEntity
import com.example.todo.model.local.entity.TodoEntity
import com.example.todo.presentation.view_model.todos.TodosUiState
import com.example.todo.presentation.view_model.todos.TodosViewModel

@Composable
fun TodosScreen(viewModel: TodosViewModel, modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TodosContent(
        modifier = modifier,
        uiState = uiState,
        onTodoClick = viewModel::onTodoClick,
        onToggleComplete = viewModel::onToggleComplete,
        onDeleteTodo = viewModel::onDeleteTodo,
        onOpenAddDialog = viewModel::onOpenAddTodoDialog,
        onDismissAddDialog = viewModel::onDismissAddTodoDialog,
        onAddTodo = viewModel::onAddTodo,
        onCloseSheet = viewModel::onCloseSheet,
        onToggleItem = viewModel::onToggleItem,
        onToggleTag = viewModel::onToggleTag,
        onCreateTag = viewModel::onCreateTag
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TodosContent(
    modifier: Modifier = Modifier,
    uiState: TodosUiState,
    onTodoClick: (TodoEntity) -> Unit,
    onToggleComplete: (TodoEntity) -> Unit,
    onDeleteTodo: (TodoEntity) -> Unit,
    onOpenAddDialog: () -> Unit,
    onDismissAddDialog: () -> Unit,
    onAddTodo: (String) -> Unit,
    onCloseSheet: () -> Unit,
    onToggleItem: (ItemEntity) -> Unit,
    onToggleTag: (Int) -> Unit,
    onCreateTag: (String, String) -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.todos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No todos yet. Tap + to add one!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.todos, key = { it.id }) { todo ->
                    TodoRow(
                        todo = todo,
                        tags = uiState.tags.filter { it.id in todo.tagIds },
                        onClick = { onTodoClick(todo) },
                        onToggleComplete = { onToggleComplete(todo) },
                        onDelete = { onDeleteTodo(todo) }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = onOpenAddDialog,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Todo")
        }
    }

    // Add todo dialog
    if (uiState.isAddTodoDialogOpen) {
        AddTodoDialog(
            onDismiss = onDismissAddDialog,
            onConfirm = onAddTodo
        )
    }

    // Item assignment bottom sheet
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (uiState.selectedTodo != null) {
        ModalBottomSheet(
            onDismissRequest = onCloseSheet,
            sheetState = sheetState
        ) {
            ManageTodoSheet(
                todo = uiState.selectedTodo,
                allTags = uiState.tags,
                allItems = uiState.allItems,
                assignedItems = uiState.selectedTodoItems,
                onToggleItem = onToggleItem,
                onToggleTag = onToggleTag,
                onCreateTag = onCreateTag
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TodoRow(
    todo: TodoEntity,
    tags: List<TagDto>,
    onClick: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = todo.isCompleted, onCheckedChange = { onToggleComplete() })
            Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
                Text(
                    text = todo.title,
                    style = if (todo.isCompleted)
                        MaterialTheme.typography.bodyLarge.copy(
                            textDecoration = TextDecoration.LineThrough,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    else MaterialTheme.typography.bodyLarge
                )
                if (tags.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tags.forEach { tag ->
                            Text(
                                text = tag.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primaryContainer,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AddTodoDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var title by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Todo") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title) },
                enabled = title.isNotBlank()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun ManageTodoSheet(
    todo: TodoEntity,
    allTags: List<TagDto>,
    allItems: List<ItemEntity>,
    assignedItems: List<ItemEntity>,
    onToggleItem: (ItemEntity) -> Unit,
    onToggleTag: (Int) -> Unit,
    onCreateTag: (String, String) -> Unit
) {
    var showCreateTagDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = todo.title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tags Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Tags", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { showCreateTagDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create Tag")
            }
        }
        
        if (allTags.isEmpty()) {
            Text(
                text = "No tags available. Tap + to create one.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.height(150.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(allTags, key = { "tag_${it.id}" }) { tag ->
                    val isAssigned = todo.tagIds.contains(tag.id)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAssigned,
                            onCheckedChange = { onToggleTag(tag.id) }
                        )
                        Text(
                            text = tag.name, 
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // Items Section
        Text(
            text = "Checklist Items", 
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (allItems.isEmpty()) {
            Text(
                text = "No items yet. Create some in the Items tab.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                items(allItems, key = { "item_${it.id}" }) { item ->
                    val isAssigned = assignedItems.any { it.id == item.id }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAssigned,
                            onCheckedChange = { onToggleItem(item) }
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(item.name, style = MaterialTheme.typography.bodyMedium)
                            if (item.description.isNotBlank()) {
                                Text(
                                    item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    if (showCreateTagDialog) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        
        AlertDialog(
            onDismissRequest = { showCreateTagDialog = false },
            title = { Text("New Tag") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        onCreateTag(name, description)
                        showCreateTagDialog = false 
                    },
                    enabled = name.isNotBlank()
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateTagDialog = false }) { Text("Cancel") }
            }
        )
    }
}
