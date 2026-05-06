package com.example.todo.data.repository

import com.example.todo.data.local.dao.UserDao
import com.example.todo.data.local.entity.UserEntity
import com.example.todo.data.session.SessionManager
import com.example.todo.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) : UserRepository {

    override suspend fun register(username: String, password: String): Result<Unit> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password must not be empty"))
        }
        val existing = userDao.findByUsername(username)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Username already taken"))
        }
        val userId = userDao.insert(UserEntity(username = username, password = password)).toInt()
        sessionManager.saveSession(userId, username)
        return Result.success(Unit)
    }

    override suspend fun login(username: String, password: String): Result<Unit> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password must not be empty"))
        }
        val user = userDao.findByUsername(username)
            ?: return Result.failure(IllegalArgumentException("User not found"))
        if (user.password != password) {
            return Result.failure(IllegalArgumentException("Incorrect password"))
        }
        sessionManager.saveSession(user.id, username)
        return Result.success(Unit)
    }
}
