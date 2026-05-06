package com.example.todo.model.repository

import com.example.todo.model.dto.MarsPhoto
import com.example.todo.model.remote.MarsApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarsRepository @Inject constructor(
    private val marsApiService: MarsApiService
) {
    suspend fun getMarsPhotos(): List<MarsPhoto> = marsApiService.getPhotos()
}
