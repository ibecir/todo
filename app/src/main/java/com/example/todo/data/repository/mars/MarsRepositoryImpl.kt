package com.example.todo.data.repository.mars

import com.example.todo.data.mapper.toDomain
import com.example.todo.data.remote.services.MarsApiService
import com.example.todo.domain.model.MarsPhoto
import com.example.todo.domain.repository.MarsRepository
import javax.inject.Inject

class MarsRepositoryImpl @Inject constructor(
    private val marsApiService: MarsApiService
) : MarsRepository {
    override suspend fun getMarsPhotos(): List<MarsPhoto> {
        return marsApiService.getPhotos().map { it.toDomain() }
    }
}