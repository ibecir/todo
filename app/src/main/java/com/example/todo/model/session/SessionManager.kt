package com.example.todo.model.session

import android.content.SharedPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(private val prefs: SharedPreferences) {

    val isLoggedIn: Boolean
        get() = prefs.getString(KEY_USERNAME, null) != null

    val loggedInUsername: String?
        get() = prefs.getString(KEY_USERNAME, null)

    fun saveSession(username: String) {
        prefs.edit().putString(KEY_USERNAME, username).apply()
    }

    fun clearSession() {
        prefs.edit().remove(KEY_USERNAME).apply()
    }

    companion object {
        private const val KEY_USERNAME = "logged_in_username"
    }
}
