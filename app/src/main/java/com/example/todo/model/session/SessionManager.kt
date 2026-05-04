package com.example.todo.model.session

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(private val prefs: SharedPreferences) {

    val isLoggedIn: Boolean
        get() = prefs.getInt(KEY_USER_ID, -1) != -1

    val loggedInUserId: Int
        get() = prefs.getInt(KEY_USER_ID, -1)

    val loggedInUsername: String?
        get() = prefs.getString(KEY_USERNAME, null)

    fun saveSession(userId: Int, username: String) {
        prefs.edit()
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USERNAME)
            .apply()
    }

    companion object {
        private const val KEY_USER_ID = "logged_in_user_id"
        private const val KEY_USERNAME = "logged_in_username"
    }
}
