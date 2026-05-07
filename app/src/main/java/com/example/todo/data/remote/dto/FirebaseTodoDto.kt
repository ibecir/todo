package com.example.todo.data.remote.dto

data class FirebaseTodoDto(
    val id: Int = 0,
    val userId: Int = 0,
    val title: String = "",
    val isCompleted: Boolean = false,
    val tagIds: List<Int> = emptyList()
)
