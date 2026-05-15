package com.example.todo.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class TodoStatsDto(
    val totalCount: Int,
    val completedCount: Int,
    val pendingCount: Int
)
