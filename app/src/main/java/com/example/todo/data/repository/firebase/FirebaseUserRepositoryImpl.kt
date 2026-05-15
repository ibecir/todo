package com.example.todo.data.repository.firebase

import com.example.todo.data.local.entity.UserEntity
import com.example.todo.data.session.SessionManager
import com.example.todo.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseUserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
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

        val userId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt().let { 
            if (it == -1) 0 else it 
        }
        val userEntity = UserEntity(
            id = userId,
            username = username,
            password = password
        )

        usersCollection.add(userEntity).await()
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

        val userEntity = snapshot.documents.first().toObject(UserEntity::class.java)
            ?: return Result.failure(IllegalStateException("Error parsing user data"))

        if (userEntity.password != password) {
            return Result.failure(IllegalArgumentException("Incorrect password"))
        }

        sessionManager.saveSession(userEntity.id, username)
        return Result.success(Unit)
    }

    override suspend fun signInWithGoogle(idToken: String): Result<Unit> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user ?: return Result.failure(Exception("Google sign-in failed"))
            
            val userId = user.uid.hashCode()
            val username = user.displayName ?: user.email ?: "Google User"
            val photoUrl = user.photoUrl?.toString()
            
            // For Google Sign-In, we still use the user entity pattern to keep consistency with the app
            val userEntity = UserEntity(
                id = userId,
                username = username,
                password = "", // No password for Google users
                profilePictureUrl = photoUrl
            )
            
            // Check if user exists in our Firestore 'users' collection
            val existing = usersCollection.whereEqualTo("id", userId).get().await()
            if (existing.isEmpty) {
                usersCollection.add(userEntity).await()
            }
            
            android.util.Log.d("FirebaseUserRepo", "Saving session for user: $username (ID: $userId, Photo: $photoUrl)")
            sessionManager.saveSession(userId, username, photoUrl)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("FirebaseUserRepo", "Google sign-in error", e)
            Result.failure(e)
        }
    }
}
