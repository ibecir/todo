package com.example.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.todo.presentation.ui.MainScreen
import com.example.todo.presentation.ui.screens.AuthScreen
import com.example.todo.presentation.view_model.auth.AuthViewModel
import com.example.todo.ui.theme.TodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
                val username by authViewModel.loggedInUsername.collectAsStateWithLifecycle()

                if (isLoggedIn) {
                    MainScreen(username = username, onLogout = { authViewModel.onLogout() })
                } else {
                    AuthScreen(viewModel = authViewModel)
                }
            }
        }
    }
}
