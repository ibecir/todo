package com.example.todo.domain.repository

interface UserRepository {
    suspend fun register(username: String, password: String): Result<Unit>
    suspend fun login(username: String, password: String): Result<Unit>
}
