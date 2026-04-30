package com.example.todo.model.dto

data class TodoStatsDto(
    val totalCount: Int,
    val completedCount: Int,
    val pendingCount: Int
)
