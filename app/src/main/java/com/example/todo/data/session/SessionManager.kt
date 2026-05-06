package com.example.todo.data.session

import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(private val prefs: SharedPreferences) {

    private val _userId = MutableStateFlow(prefs.getInt(KEY_USER_ID, -1))
    val userId: StateFlow<Int> = _userId.asStateFlow()

    private val _username = MutableStateFlow(prefs.getString(KEY_USERNAME, null))
    val username: StateFlow<String?> = _username.asStateFlow()

    val isLoggedIn: Boolean
        get() = userId.value != -1

    val loggedInUserId: Int
        get() = userId.value

    val loggedInUsername: String?
        get() = username.value

    fun saveSession(userId: Int, username: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .apply()
        _userId.value = userId
        _username.value = username
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .apply()
        _userId.value = -1
        _username.value = null
    }

    companion object {
        private const val KEY_USER_ID = "logged_in_user_id"
        private const val KEY_USERNAME = "logged_in_username"
    }
}
