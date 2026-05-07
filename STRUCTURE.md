# Project Structure & Architecture

This document describes the directory layout and architectural patterns used in the Todo application. The project follows **Clean Architecture** principles organized by **Features**.

## 1. High-Level Architecture: The "Why"

While it might seem like there are "extra steps," this architecture is designed for **flexibility, testability, and long-term maintenance.** 

By separating the app into distinct layers, we ensure that the "Business Logic" (how your app works) is independent of "Infrastructure" (the database, the internet, or the UI framework).

---

## 2. The Three Layers

### 🏛️ Domain Layer (`domain/`)
**The "Brain" of the App.**
- **Domain Models:** Pure Kotlin classes (e.g., `Todo`, `Item`). These are the **"Universal Translators."** They have no knowledge of Room, Retrofit, or Android.
- **Repository Interfaces:** The "Contracts." They define *what* the app can do (e.g., `fun getAllItems()`) without specifying *how* (e.g., SQL vs JSON).
- **Benefit:** If you want to change how data is stored, you never have to touch this layer.

### 🔌 Data Layer (`data/`)
**The "Worker" of the App.**
- **Entities & DTOs:** Models specific to the database (`ItemEntity`) or API (`ItemDto`).
- **Repositories Implementation:** These classes "plug into" the Domain interfaces. They are responsible for fetching data and **mapping** it into Domain Models.
- **Mappers:** The bridge that converts `Entity` ↔ `Domain Model`.
- **Benefit:** All the "messy" details of SQL queries or Network errors are hidden here.

### 📱 Presentation Layer (`ui/`)
**The "Face" of the App.**
- **ViewModels:** They ask for a Repository **interface**. They don't care if the data comes from a local file or a server in space.
- **Compose UI:** Renders the `UiState`. It only ever sees **Domain Models**.
- **Benefit:** The UI stays clean and focused only on showing data, not managing it.

---

## 3. The "Universal Translator" (Domain Models)

Imagine your app as a global hub:
1.  **The Database** speaks "Table Rows" (`ItemEntity`).
2.  **The Internet** speaks "JSON" (`ItemDto`).
3.  **The UI** speaks "Clean Kotlin" (`Item`).

The **Repository** acts as the translator. Because the `ViewModel` and `UI` only ever see the "Clean Kotlin" version, they are protected from changes in the underlying data sources.

---

## 4. Dependency Injection: The "Switchboard"

We use **Hilt** to manage how these layers connect. The file `di/RepositoryModule.kt` acts as the master switchboard.

### The "Swapping Database" Scenario
If we need to move from **Local Room** storage to a **Remote Cloud API**:
1.  **Old Way (Hard):** You'd have to rewrite every ViewModel and UI screen.
2.  **Clean Way (Easy):** 
    - Create a new implementation of the Repository interface (e.g., `ItemRepositoryRemoteImpl`).
    - Change **one line** in `RepositoryModule.kt` to point to the new class.
    - **Result:** The ViewModels and UI continue to work perfectly without knowing anything changed.

---

## 5. Directory Layout

```text
com.example.todo/
├── data/                    # DATA LAYER (Implementation)
│   ├── local/               # Room database, DAOs, and Entities
│   ├── remote/              # Retrofit API interfaces and DTOs
│   ├── repository/          # The "Workers" that implement the Domain contracts
│   ├── mapper/              # Mappers (Conversion between Entity/DTO and Domain)
│   └── session/             # Local session management (SharedPreferences)
│
├── domain/                  # DOMAIN LAYER (Business Logic)
│   ├── model/               # The "Universal Translator" models
│   └── repository/          # The "Contracts" (Interfaces)
│
├── ui/                      # PRESENTATION LAYER (UI)
│   ├── features/            # Feature-based organization (auth, todos, items, etc.)
│   ├── navigation/          # Compose Navigation (NavGraph, Screen routes)
│   ├── common/              # Shared UI components
│   └── theme/               # Design system (Color, Typography)
│
├── di/                      # THE SWITCHBOARD (Hilt Modules)
└── MainActivity.kt          # Entry point
```

---

## 6. Key Conventions

1.  **No Leaky Abstractions:** Never use an `Entity` or `Dto` in a ViewModel or Screen. Always map to `Domain`.
2.  **UDF (Unidirectional Data Flow):** Events go UP to the ViewModel; State flows DOWN to the UI.
3.  **Reactive Architecture:** Use `Flow` to ensure the UI updates automatically when data changes in the database.
