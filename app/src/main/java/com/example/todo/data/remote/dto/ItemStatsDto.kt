package com.example.todo.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemStatsDto(
    val totalCount: Int,
    val assignedCount: Int
)
