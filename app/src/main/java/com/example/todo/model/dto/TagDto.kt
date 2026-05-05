package com.example.todo.model.dto

import com.google.gson.annotations.SerializedName

data class TagDto(
    val id: Int,
    val name: String,
    val description: String,
    @SerializedName("user_id") val userId: Int
)

data class CreateTagRequest(
    val name: String,
    val description: String,
    @SerializedName("user_id") val userId: Int
)
