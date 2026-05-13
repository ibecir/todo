package com.example.todo.ui.features

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.todo.ui.features.items.ItemsScreen
import com.example.todo.ui.features.items.ItemsViewModel
import com.example.todo.ui.features.mars.MarsPhotosScreen
import com.example.todo.ui.features.mars.MarsViewModel
import com.example.todo.ui.features.stats.StatsScreen
import com.example.todo.ui.features.stats.StatsViewModel
import com.example.todo.ui.features.todos.TodosScreen
import com.example.todo.ui.features.todos.TodosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    username: String?,
    profilePictureUrl: String?,
    onLogout: () -> Unit
) {
    val todosViewModel: TodosViewModel = hiltViewModel()
    val itemsViewModel: ItemsViewModel = hiltViewModel()
    val statsViewModel: StatsViewModel = hiltViewModel()
    val marsViewModel: MarsViewModel = hiltViewModel()

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            0 -> "My Todos"
                            1 -> "Items"
                            2 -> "Stats"
                            else -> "Mars Photos"
                        }
                    )
                },
                actions = {
                    if (username != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            if (profilePictureUrl != null) {
                                AsyncImage(
                                    model = profilePictureUrl,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = username,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        },
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
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Face, contentDescription = null) }, // Using Face for Mars
                    label = { Text("Mars") }
                )
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> TodosScreen(viewModel = todosViewModel, modifier = Modifier.padding(padding))
            1 -> ItemsScreen(viewModel = itemsViewModel, modifier = Modifier.padding(padding))
            2 -> StatsScreen(viewModel = statsViewModel, modifier = Modifier.padding(padding))
            3 -> MarsPhotosScreen(viewModel = marsViewModel, modifier = Modifier.padding(padding))
        }
    }
}
