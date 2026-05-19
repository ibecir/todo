package com.example.todo.ui.features.stats

import com.example.todo.domain.model.ItemStats
import com.example.todo.domain.model.TodoStats

data class StatsUiState(
    val todoStats: TodoStats = TodoStats(0, 0, 0),
    val itemStats: ItemStats = ItemStats(0, 0),
    val isExporting: Boolean = false,
    val exportMessage: String? = null,
    val exportUri: String? = null
)
