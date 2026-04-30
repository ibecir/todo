package com.example.todo.presentation.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.todo.presentation.ui.screens.ItemsScreen
import com.example.todo.presentation.ui.screens.StatsScreen
import com.example.todo.presentation.ui.screens.TodosScreen
import com.example.todo.presentation.view_model.items.ItemsViewModel
import com.example.todo.presentation.view_model.stats.StatsViewModel
import com.example.todo.presentation.view_model.todos.TodosViewModel

@Composable
fun MainScreen() {
    val todosViewModel: TodosViewModel = hiltViewModel()
    val itemsViewModel: ItemsViewModel = hiltViewModel()
    val statsViewModel: StatsViewModel = hiltViewModel()

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Todos") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                    label = { Text("Items") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Info, contentDescription = null) },
                    label = { Text("Stats") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> TodosScreen(viewModel = todosViewModel, modifier = Modifier.padding(padding))
            1 -> ItemsScreen(viewModel = itemsViewModel, modifier = Modifier.padding(padding))
            2 -> StatsScreen(viewModel = statsViewModel, modifier = Modifier.padding(padding))
        }
    }
}
