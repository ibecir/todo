package com.example.todo.model.repository

import com.example.todo.model.local.dao.UserDao
import com.example.todo.model.local.entity.UserEntity
import com.example.todo.model.session.SessionManager
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {

    suspend fun register(username: String, password: String): Result<Unit> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password must not be empty"))
        }
        val existing = userDao.findByUsername(username)
        if (existing != null) {
            return Result.failure(IllegalArgumentException("Username already taken"))
        }
        userDao.insert(UserEntity(username = username, password = password))
        sessionManager.saveSession(username)
        return Result.success(Unit)
    }

    suspend fun login(username: String, password: String): Result<Unit> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password must not be empty"))
        }
        val user = userDao.findByUsername(username)
            ?: return Result.failure(IllegalArgumentException("User not found"))
        if (user.password != password) {
            return Result.failure(IllegalArgumentException("Incorrect password"))
        }
        sessionManager.saveSession(username)
        return Result.success(Unit)
    }
}
