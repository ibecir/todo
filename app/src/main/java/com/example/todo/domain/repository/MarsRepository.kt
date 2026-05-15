package com.example.todo.domain.repository

import com.example.todo.domain.model.MarsPhoto

interface MarsRepository {
    suspend fun getMarsPhotos(): List<MarsPhoto>
}
