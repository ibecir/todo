# Todo App Architecture & Guidelines

This document provides an overview of the Todo App's architecture, data flow, and coding conventions. It serves as a guide for AI assistants and developers working on this codebase.

## 1. Architecture Overview

The project follows an **MVVM (Model-View-ViewModel)** architecture combined with principles of **Clean Architecture**. It relies heavily on Kotlin Coroutines and Flow for reactive programming.

### Technologies
*   **UI:** Jetpack Compose
*   **Architecture Components:** ViewModel, Navigation Compose
*   **Dependency Injection:** Dagger Hilt
*   **Database:** Room
*   **Concurrency:** Kotlin Coroutines & Flow

### Directory Structure
```
app/src/main/java/com/example/todo/
├── di/                  # Dagger Hilt modules (AppModule, DatabaseModule)
├── model/               # Data Layer
│   ├── dto/             # Data Transfer Objects (e.g., Stats)
│   ├── local/           # Room Database, DAOs, and Entities
│   ├── repository/      # Repository implementations acting as the single source of truth
│   └── session/         # SessionManager for reactive user state
├── presentation/        # UI Layer
│   ├── navigation/      # Compose Navigation Graph and Routes
│   ├── ui/              # Compose UI elements (MainScreen, screens/)
│   └── view_model/      # ViewModels categorized by feature/screen
└── MainActivity.kt      # Application entry point
```

## 2. Data Flow & State Management

The application adheres to a strict unidirectional data flow (UDF) and reactive state management pattern.

1.  **Session State as the Source of Truth:** The `SessionManager` exposes the current `userId` as a `StateFlow`. This is the fundamental piece of state that drives the rest of the application.
2.  **Reactive ViewModels:** ViewModels do **not** read the `userId` once during initialization. Instead, they observe the `userId` flow from `SessionManager` using `flatMapLatest`.
3.  **Data Fetching:** When the `userId` changes, the ViewModels dynamically swap their underlying Room database streams (`Flow<List<T>>`) by calling Repository methods.
4.  **UI State Mapping:** The flows from the Repositories are mapped into specific `UiState` data classes (e.g., `TodosUiState`) and exposed to the Compose UI as `StateFlow`.
5.  **UI Rendering:** Compose UI functions observe these `StateFlow`s using `collectAsStateWithLifecycle()` and render the UI based on the current state.
6.  **User Actions:** User actions in the UI trigger methods on the ViewModel (e.g., `onToggleComplete()`), which then call suspend functions on the Repository to modify the database. Room automatically emits new values to the active Flows, completing the cycle.

### Reactive Example (TodosViewModel)
```kotlin
sessionManager.userId.flatMapLatest { userId ->
    if (userId != -1) todoRepository.getTodos(userId) else flowOf(emptyList())
}.collect { todos ->
    _uiState.update { it.copy(todos = todos) }
}
```

## 3. Core Components

### Room Database (`model/local/`)
*   Contains entities: `UserEntity`, `TodoEntity`, `ItemEntity`, and the associative `TodoItems`.
*   DAOs (`TodoDao`, `ItemDao`, `UserDao`) expose data as `Flow<T>` for reactive reading and provide `suspend` functions for writing.
*   **Security Rule:** All data access queries **must** include a `userId` filter to prevent data leakage between sessions (e.g., `WHERE userId = :userId`).
*   **Schema Convention:** Associative/junction entities (like `TodoItems`) should use a surrogate, auto-generated `id` as the primary key rather than a composite key, combined with a unique index to prevent duplicate relations.

### Repositories (`model/repository/`)
*   Act as intermediaries between the ViewModels and the DAOs.
*   They encapsulate the data access logic and provide a clean API for the presentation layer.

### ViewModels (`presentation/view_model/`)
*   Scoped using `@HiltViewModel`.
*   Maintain screen-specific state using `MutableStateFlow` wrapped in a `UiState` data class.
*   Handle navigation events via a `Channel` exposed as a `Flow` to prevent event loss during configuration changes.

### Navigation (`presentation/navigation/`)
*   `NavGraph` defines the routing logic for the application.
*   The `MainActivity` decides the root destination based on the `isLoggedIn` state from `AuthViewModel`.

## 4. Coding Conventions & Best Practices

*   **Reactive Session:** Always react to `SessionManager.userId` changes using Flow operators. Never store the `userId` as a static value in a ViewModel's `init` block, as ViewModels may outlive a user session due to Activity scoping.
*   **Data Isolation:** Every Room query involving user data MUST explicitly filter by the logged-in user's ID to prevent cross-account data leakage.
*   **Unidirectional Data Flow:** UI components should never modify state directly. They must emit events to the ViewModel, which then updates the state or database.
*   **State Hoisting:** Keep Compose UI components stateless where possible, passing down state and hoisting events up to the screen-level Composable or ViewModel.
*   **Lifecycle-Aware Collection:** Always use `collectAsStateWithLifecycle()` in Compose UI to collect ViewModel state flows safely.
*   **Navigation Events:** Use a buffered `Channel` for one-off events like navigation or showing snackbars in ViewModels.
