package com.example.todo.ui.features.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.todo.domain.model.ItemStats
import com.example.todo.domain.model.TodoStats

import android.content.Intent
import android.net.Uri
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel(), modifier: Modifier = Modifier) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.exportMessage) {
        uiState.exportMessage?.let { message ->
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = if (uiState.exportUri != null) "Open" else null,
                duration = SnackbarDuration.Long
            )
            
            if (result == SnackbarResult.ActionPerformed && uiState.exportUri != null) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(Uri.parse(uiState.exportUri), "text/csv")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Open with"))
            }
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        StatsContent(
            modifier = Modifier.padding(padding),
            todoStats = uiState.todoStats,
            itemStats = uiState.itemStats,
            isExporting = uiState.isExporting,
            onExportClick = { viewModel.onExportStats() }
        )
    }
}

@Composable
private fun StatsContent(
    modifier: Modifier = Modifier,
    todoStats: TodoStats,
    itemStats: ItemStats,
    isExporting: Boolean,
    onExportClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Todos section
        item {
            SectionHeader(title = "Todos")
        }
        item {
            val total = todoStats.totalCount
            val completed = todoStats.completedCount
            val pending = todoStats.pendingCount
            val progress = if (total > 0) completed.toFloat() / total else 0f

            StatGroupCard {
                StatRow(label = "Total", value = total.toString())
                StatRow(label = "Completed", value = completed.toString())
                StatRow(label = "Pending", value = pending.toString())
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Completion rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Items section
        item {
            SectionHeader(title = "Items")
        }
        item {
            val total = itemStats.totalCount
            val assigned = itemStats.assignedCount
            val unassigned = total - assigned
            val progress = if (total > 0) assigned.toFloat() / total else 0f

            StatGroupCard {
                StatRow(label = "Total", value = total.toString())
                StatRow(label = "Assigned to a todo", value = assigned.toString())
                StatRow(label = "Unassigned", value = unassigned.toString())
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Assignment rate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Actions section
        item {
            SectionHeader(title = "Actions")
        }
        item {
            Button(
                onClick = onExportClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExporting,
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isExporting) "Exporting..." else "Export to CSV")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun StatGroupCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
    }
}
