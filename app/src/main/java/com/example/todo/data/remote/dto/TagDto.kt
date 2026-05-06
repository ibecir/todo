package com.example.todo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagDto(
    val id: Int,
    val name: String,
    val description: String,
    @SerialName("user_id") val userId: Int
)

@Serializable
data class CreateTagRequest(
    val name: String,
    val description: String,
    @SerialName("user_id") val userId: Int
)
