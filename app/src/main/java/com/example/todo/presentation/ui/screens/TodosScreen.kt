package com.example.todo.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
        onToggleItem = viewModel::onToggleItem
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
    onToggleItem: (ItemEntity) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("My Todos") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "Add Todo")
            }
        }
    ) { padding ->
        if (uiState.todos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
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
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.todos, key = { it.id }) { todo ->
                    TodoRow(
                        todo = todo,
                        onClick = { onTodoClick(todo) },
                        onToggleComplete = { onToggleComplete(todo) },
                        onDelete = { onDeleteTodo(todo) }
                    )
                }
            }
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
            ItemAssignmentSheet(
                todo = uiState.selectedTodo,
                allItems = uiState.allItems,
                assignedItems = uiState.selectedTodoItems,
                onToggleItem = onToggleItem
            )
        }
    }
}

@Composable
private fun TodoRow(
    todo: TodoEntity,
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
            Text(
                text = todo.title,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                style = if (todo.isCompleted)
                    MaterialTheme.typography.bodyLarge.copy(
                        textDecoration = TextDecoration.LineThrough,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                else MaterialTheme.typography.bodyLarge
            )
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
private fun ItemAssignmentSheet(
    todo: TodoEntity,
    allItems: List<ItemEntity>,
    assignedItems: List<ItemEntity>,
    onToggleItem: (ItemEntity) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = todo.title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Select items to assign",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
        if (allItems.isEmpty()) {
            Text(
                text = "No items yet. Create some in the Items tab first.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
                items(allItems, key = { it.id }) { item ->
                    val isAssigned = assignedItems.any { it.id == item.id }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isAssigned,
                            onCheckedChange = { onToggleItem(item) }
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(item.name, style = MaterialTheme.typography.bodyLarge)
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
}
