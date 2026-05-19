package com.example.todo.domain.repository

import com.example.todo.domain.model.ItemStats
import com.example.todo.domain.model.TodoStats

interface StatsRepository {
    suspend fun exportStats(todoStats: TodoStats, itemStats: ItemStats): Result<String>
}
