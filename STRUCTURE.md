# Project Structure & Architecture

This document describes the directory layout and architectural patterns used in the Todo application. The project follows **Clean Architecture** principles organized by **Features**.

## 1. High-Level Architecture

The application is divided into three primary layers to ensure a strict separation of concerns:

-   **Data Layer (`data/`):** Implementation details for data persistence (Room) and remote communication (Retrofit).
-   **Domain Layer (`domain/`):** Pure business logic. Contains the "Source of Truth" models and Repository interfaces.
-   **Presentation Layer (`ui/`):** The UI layer using Jetpack Compose and ViewModels.

---

## 2. Directory Layout

```text
com.example.todo/
├── data/                    # DATA LAYER
│   ├── local/               # Room database, DAOs, and Entities
│   ├── remote/              # Retrofit API interfaces and DTOs
│   ├── repository/          # Implementation of domain repository interfaces
│   ├── mapper/              # Mappers (Conversion between Entity/DTO and Domain)
│   └── session/             # Local session management (SharedPreferences)
│
├── domain/                  # DOMAIN LAYER
│   ├── model/               # Pure Kotlin POJOs (The "Domain Models")
│   └── repository/          # Repository interfaces (The "Contracts")
│
├── ui/                      # PRESENTATION LAYER
│   ├── features/            # Feature-based organization
│   │   ├── auth/            # Authentication (Login/Register)
│   │   ├── todos/           # Main Todo list management
│   │   ├── todo_detail/     # Detailed view of a single Todo
│   │   ├── items/           # Global Item management
│   │   ├── stats/           # User statistics
│   │   └── mars/            # External API integration (Mars Photos)
│   ├── navigation/          # Compose Navigation (NavGraph, Screen routes)
│   ├── common/              # Shared UI components (Stateless)
│   └── theme/               # Design system (Color, Shape, Typography)
│
├── di/                      # Dependency Injection (Hilt Modules)
└── MainActivity.kt          # Entry point and Root UI decision logic
```

---

## 3. Data Flow & Model Naming

To prevent technical details from leaking into the UI, we use distinct models for different purposes:

### Local Persistence (`data/local/entity/`)
-   **Suffix:** `Entity` (e.g., `TodoEntity`).
-   **Role:** Annotated with Room attributes. Used strictly for database rows.

### Remote Communication (`data/remote/dto/`)
-   **Suffix:** `Dto` (e.g., `TagDto`, `MarsPhotoDto`).
-   **Role:** Annotated with `@Serializable`. Used strictly for JSON serialization/deserialization.

### Pure Business Logic (`domain/model/`)
-   **Suffix:** None (e.g., `Todo`, `Tag`, `MarsPhoto`).
-   **Role:** Pure Kotlin data classes. This is the **only** model type that should reach the ViewModels and UI.

---

## 4. Layer Responsibilities

### Data Layer
-   Performs CRUD operations on the database.
-   Handles network exceptions and HTTP status codes.
-   Uses **Mappers** to convert technical models (`Entity`/`Dto`) into `Domain` models before returning them to the higher layers.

### Domain Layer
-   Defines the business models that the UI depends on.
-   Defines repository interfaces that specify *what* data is needed without caring *where* it comes from.
-   Contains Use Cases (optional, added as complexity grows).

### Presentation Layer
-   **ViewModels:** Observe reactive streams from Repositories and maintain a single `UiState`.
-   **Screens:** Stateless Compose functions that render the `UiState`.
-   **Navigation:** Uses `NavGraph` to coordinate transitions between features.

---

## 5. Key Conventions

1.  **Reactive State:** All data access should ideally return a `Flow<T>`.
2.  **Unidirectional Data Flow:** UI triggers events → ViewModel updates state → UI renders state.
3.  **Hilt for DI:** All dependencies are provided via Hilt. Use `@Binds` in `RepositoryModule` to link interfaces to implementations.
4.  **No Leaky Abstractions:** Never use a `TodoEntity` or `TagDto` inside a Composable or a ViewModel. Always map to a `domain/model` first.
