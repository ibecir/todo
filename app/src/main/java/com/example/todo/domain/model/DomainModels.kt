package com.example.todo.domain.model

data class Todo(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val tagIds: List<Int> = emptyList()
)

data class Item(
    val id: Int = 0,
    val userId: Int,
    val name: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class User(
    val id: Int = 0,
    val username: String
)

data class Tag(
    val id: Int,
    val name: String,
    val description: String,
    val userId: Int
)

data class TodoStats(
    val totalCount: Int,
    val completedCount: Int,
    val pendingCount: Int
)

data class ItemStats(
    val totalCount: Int,
    val assignedCount: Int // Number of times items are assigned to todos
)

data class MarsPhoto(
    val id: String,
    val imgSrc: String
)
