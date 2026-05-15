package com.example.todo.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MarsPhotoDto(
    val id: String,
    @SerialName(value = "img_src")
    val imgSrc: String
)
