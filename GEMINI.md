# Todo App Architecture & Guidelines

This document provides an overview of the Todo App's architecture, data flow, and coding conventions. It serves as a guide for developers and AI assistants working on this codebase.

## 1. Architecture Overview (Feature-Based Clean Architecture)

The project follows a **Clean Architecture** pattern organized by **Features**. It decouples the business logic (Domain) from the UI (Presentation) and the data sources (Data).

### Directory Structure
```
app/src/main/java/com/example/todo/
├── data/                    # Data Layer (Implementation)
│   ├── local/               # Room Database, DAOs, and Entities
│   ├── remote/              # Retrofit APIs and DTOs
│   ├── repository/          # Concrete Repository Implementations
│   ├── mapper/              # Mappers (Entity/Dto <-> Domain Model)
│   └── session/             # SessionManager (SharedPreferences)
├── domain/                  # Domain Layer (Pure Business Logic)
│   ├── model/               # Domain Models (Pure Kotlin POJOs)
│   └── repository/          # Repository Interfaces
├── ui/                      # Presentation Layer (Jetpack Compose)
│   ├── features/            # Feature-based packages (auth, todos, items, etc.)
│   ├── navigation/          # Navigation Graph and Routes
│   └── theme/               # UI Theme (Color, Typography, etc.)
├── di/                      # Dependency Injection (Hilt Modules)
└── MainActivity.kt          # Application entry point
```

## 2. Detailed Data Flow

The application follows a strict unidirectional data flow (UDF) through multiple layers.

### Path A: Data Retrieval (Reactive)
1.  **Data Source:** Room emits a `List<TodoEntity>` or Retrofit returns a `TagDto`.
2.  **Mapper:** The `data/mapper/Mappers.kt` functions (e.g., `toDomain()`) convert these into pure `domain/model` objects (e.g., `Todo`, `Tag`).
3.  **Repository:** The `data/repository/TodoRepositoryImpl` collects the raw data flow, maps it to domain models, and exposes it as a `Flow<List<Todo>>`.
4.  **ViewModel:** ViewModels (e.g., `TodosViewModel`) inject the **interface** (`domain/repository/TodoRepository`). They observe the flow and update a `MutableStateFlow<TodosUiState>`.
5.  **UI:** Compose functions observe the state using `collectAsStateWithLifecycle()` and render the domain models.

### Path B: Data Modification
1.  **User Action:** User clicks "Complete" in the UI.
2.  **ViewModel:** Calls a method like `onToggleComplete(todo: Todo)`.
3.  **Repository:** The ViewModel passes the domain model to the Repository. The Repository maps it back to an entity (`toEntity()`) and calls the DAO's `update()` method.
4.  **Database:** Room updates the record, which automatically triggers a new emission in the reactive Flow from Path A.

## 3. Layer Responsibilities

### Data Layer (`data/`)
*   **Entities & DTOs:** All classes used for database or network communication MUST be kept in the data layer.
*   **Consistency:** All Network DTOs use `@Serializable` and `@SerialName` for explicit mapping.
*   **Encapsulation:** Data layer models (Entities/DTOs) MUST NEVER leak into the ViewModel or UI. They must be mapped to Domain Models before leaving the Repository.

### Domain Layer (`domain/`)
*   **Domain Models:** Simple, stable Kotlin data classes. They have no dependencies on Android or external libraries (except basic ones like `kotlinx.coroutines` for interfaces).
*   **Repositories:** Define the *what*, not the *how*. Interfaces here allow for easy swapping of data sources or mocking in tests.

### Presentation Layer (`ui/`)
*   **Feature Packages:** Every feature contains its own Screen, ViewModel, and UiState.
*   **State Management:** Use `MutableStateFlow` in ViewModels and expose as `StateFlow`.
*   **Navigation:** One-off events (like navigating or showing a snackbar) are handled via a buffered `Channel` exposed as a `Flow`.

## 4. Coding Conventions & Security

*   **Data Isolation:** Every Room query involving user data MUST explicitly filter by the logged-in user's ID (`WHERE userId = :userId`) to prevent cross-account data leakage.
*   **Reactive Session:** ViewModels MUST react to `SessionManager.userId` changes using `flatMapLatest`. Never store the `userId` as a static value in a ViewModel's `init` block.
*   **UDF:** UI components should never modify state directly. They must emit events to the ViewModel.
*   **Stateless UI:** Keep Composables stateless by passing down data and hoisting events up to the ViewModel.
