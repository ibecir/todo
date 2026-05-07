package com.example.todo.data.repository

import com.example.todo.data.remote.dto.FirebaseUserDto
import com.example.todo.data.session.SessionManager
import com.example.todo.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager
) : UserRepository {

    private val usersCollection = firestore.collection("users")

    override suspend fun register(username: String, password: String): Result<Unit> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password must not be empty"))
        }

        val existing = usersCollection.whereEqualTo("username", username).get().await()
        if (!existing.isEmpty) {
            return Result.failure(IllegalArgumentException("Username already taken"))
        }

        val userId = System.currentTimeMillis().toInt()
        val userDto = FirebaseUserDto(
            id = userId,
            username = username,
            password = password
        )

        usersCollection.add(userDto).await()
        sessionManager.saveSession(userId, username)
        return Result.success(Unit)
    }

    override suspend fun login(username: String, password: String): Result<Unit> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(IllegalArgumentException("Username and password must not be empty"))
        }

        val snapshot = usersCollection.whereEqualTo("username", username).get().await()
        if (snapshot.isEmpty) {
            return Result.failure(IllegalArgumentException("User not found"))
        }

        val userDto = snapshot.documents.first().toObject(FirebaseUserDto::class.java)
            ?: return Result.failure(IllegalStateException("Error parsing user data"))

        if (userDto.password != password) {
            return Result.failure(IllegalArgumentException("Incorrect password"))
        }

        sessionManager.saveSession(userDto.id, username)
        return Result.success(Unit)
    }
}
