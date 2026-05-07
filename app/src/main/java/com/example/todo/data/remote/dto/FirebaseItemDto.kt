package com.example.todo.data.remote.dto

data class FirebaseItemDto(
    val id: Int = 0,
    val userId: Int = 0,
    val name: String = "",
    val description: String = "",
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)
