package com.example.todo.presentation.view_model.stats

import com.example.todo.model.dto.ItemStatsDto
import com.example.todo.model.dto.TodoStatsDto

data class StatsUiState(
    val todoStats: TodoStatsDto? = null,
    val itemStats: ItemStatsDto? = null
)
