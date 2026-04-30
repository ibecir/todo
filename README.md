# Todo App

A simple Todo application built with Kotlin, Jetpack Compose, Room, Hilt, and Coroutines — following the MVVM + Repository pattern taught in the Mobile Programming 2026 course.

---

## Project Architecture

### Layers at a glance

```
UI (Compose screens)
    ↕  StateFlow / NavigationEvent
ViewModel
    ↕  suspend functions / Flow
Repository
    ↕  suspend functions / Flow
DAO
    ↕  SQLite (Room generates the implementation)
Database (Room)
```

---

### 1. Room / DAO — the database layer

**`TodoDatabase`** is an abstract class. Room generates a concrete implementation at compile time (via KSP) that handles SQLite connections, thread safety, and query execution.

**`TodoDao`** is an interface. Every method has two shapes:

```kotlin
// Returns a Flow — NOT suspend. Room keeps the query "alive".
@Query("SELECT * FROM todos ORDER BY id DESC")
fun getAllTodos(): Flow<List<TodoEntity>>

// suspend — runs once, then returns.
@Insert suspend fun insert(todo: TodoEntity)
@Update suspend fun update(todo: TodoEntity)
@Delete suspend fun delete(todo: TodoEntity)
```

**Why `Flow` for the query, `suspend` for writes?**

- `Flow` is a *stream*. Room emits a new list every time the table changes. The subscriber (Repository → ViewModel) always has a live view of the data without polling.
- `suspend` for writes means "do this one async operation, then I'm done." No ongoing stream needed.

**Threading**: Room's generated code automatically executes all database work on an internal `Dispatchers.IO` thread pool. You never have to say `withContext(Dispatchers.IO)` yourself for Room operations.

---

### 2. Repository — the single source of truth

```kotlin
class TodoRepository @Inject constructor(private val todoDao: TodoDao) {
    val todos: Flow<List<TodoEntity>> = todoDao.getAllTodos()

    suspend fun insert(todo: TodoEntity) = todoDao.insert(todo)
    suspend fun update(todo: TodoEntity) = todoDao.update(todo)
    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)
}
```

It's a thin wrapper here, but its role is important:

- **Hides the data source** from the ViewModel. The ViewModel doesn't know or care that data comes from Room. If you later add a network sync, you swap logic here only.
- **`val todos`** is initialised once — it's just the DAO's `Flow` re-exposed. No data is fetched at construction time; the Flow is cold until collected.
- All `suspend` functions simply delegate to the DAO. The DAO is already on `IO`, so no explicit dispatcher switching needed.

---

### 3. ViewModel — state and business logic

Each screen has its own ViewModel. Both follow the exact same structure:

```
@HiltViewModel
class TodoListViewModel @Inject constructor(repository) : ViewModel()
    │
    ├── _uiState: MutableStateFlow       ← what the UI renders
    ├── _navigationEvent: Channel        ← one-shot navigation triggers
    │
    ├── init { viewModelScope.launch { repository.todos.collect {...} } }
    └── fun onDelete / onToggle / onAdd  → viewModelScope.launch { repository.xxx() }
```

**`viewModelScope`** is a `CoroutineScope` tied to the ViewModel's lifecycle. When the ViewModel is cleared (screen permanently gone), all coroutines in this scope are cancelled automatically. You never leak a running database query.

**Dispatcher context for ViewModels**: `viewModelScope` runs on `Dispatchers.Main` by default. This is intentional — updating `StateFlow` from a background thread would require thread-safe handling. Instead:

```kotlin
viewModelScope.launch {          // Main thread
    repository.todos.collect {   // Flow emission arrives here...
        _uiState.value = ...     // ...safely updated on Main
    }
}
```

Room emits on its IO thread, but coroutines' `collect` automatically delivers the value to the calling coroutine's context (Main here). No manual `withContext` needed.

For writes:
```kotlin
viewModelScope.launch {          // Main
    repository.delete(todo)      // suspends → Room switches to IO internally
                                 // resumes here on Main when done
}
```

---

### 4. Two kinds of state: `StateFlow` vs `Channel`

These solve two different problems:

**`StateFlow<UiState>`** — *what is the current screen state?*

```kotlin
private val _uiState = MutableStateFlow<TodoListUiState>(TodoListUiState.Loading)
val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()
```

- Always holds a value (starts with `Loading`).
- New subscribers immediately get the latest value — like a LiveData.
- The UI collects this and re-renders whenever it changes.
- Perfect for: loading spinners, list content, error messages.

**`Channel<NavigationEvent>`** — *did something happen once?*

```kotlin
private val _navigationEvent = Channel<NavigationEvent>(Channel.BUFFERED)
val navigationEvent: Flow<NavigationEvent> = _navigationEvent.receiveAsFlow()
```

- Delivers events exactly once, even if the UI subscribes late (`BUFFERED` queues them).
- After consumed, the event is gone — no risk of navigating twice on re-subscription.
- Perfect for: navigate to screen, show snackbar, close screen.

If you used `StateFlow` for navigation, a screen rotation would re-trigger the navigation because the subscriber would re-receive the last value.

---

### 5. UI layer — Compose screens

Each screen follows a **container + content** split:

```
TodoListScreen()                        ← container: owns ViewModel, handles side effects
    │
    ├── collectAsStateWithLifecycle()   ← observes StateFlow, lifecycle-safe
    ├── LaunchedEffect(Unit)            ← collects navigation Channel once
    │
    └── TodoListContent()              ← pure UI, no ViewModel reference
            ├── TodoItem()
            └── ...
```

**`collectAsStateWithLifecycle()`** vs plain `collectAsState()`:**
The lifecycle-aware version stops collecting when the app goes to background, preventing wasted recompositions and battery drain.

**`LaunchedEffect(Unit)`** runs once when the composable enters the composition and cancels when it leaves. It's where the navigation Channel is collected — ensuring it lives as long as the screen is visible.

---

### 6. Dependency injection (Hilt)

The wiring chain is:

```
@HiltAndroidApp TodoApp         ← creates the Hilt component graph at app start
    │
    └── DatabaseModule          ← @Singleton: one Database, one DAO for the app's life
            provides TodoDatabase
            provides TodoDao
            │
            └── TodoRepository  ← @Inject constructor, Hilt knows how to build it
                    │
                    └── TodoListViewModel / AddTodoViewModel  ← @HiltViewModel
                            │
                            └── injected via hiltViewModel() in each screen
```

`@Singleton` on the Database means Room opens the SQLite file once and reuses the same connection everywhere. If it weren't singleton, you'd get multiple database handles which can cause corruption.

---

### Data flow for a full user interaction

**User checks off a todo:**

```
1.  User taps Checkbox in TodoItem (UI)
2.  onToggleComplete(todo) callback fires
3.  TodoListViewModel.onToggleComplete(todo) called (Main thread)
4.  viewModelScope.launch { ... }
5.  repository.update(todo.copy(isCompleted = true))
6.  todoDao.update(...) — Room switches to IO thread, executes UPDATE SQL
7.  Room detects table change, emits new List<TodoEntity> on the Flow
8.  ViewModel's collect{} receives the new list (back on Main)
9.  _uiState.value = TodoListUiState.Success(newList)
10. StateFlow notifies collector in Compose
11. collectAsStateWithLifecycle() triggers recomposition
12. LazyColumn redraws with updated item (strikethrough text)
```

The whole round-trip is reactive — you never manually refresh the list.

---

## Tech stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| State management | StateFlow + Channel (Coroutines) |
| Dependency injection | Hilt |
| Database | Room |
| Async | Kotlin Coroutines |
| Annotation processing | KSP |

## Package structure

```
com.example.todo/
├── TodoApp.kt                              @HiltAndroidApp Application
├── MainActivity.kt                         @AndroidEntryPoint, NavGraph entry
├── di/
│   └── DatabaseModule.kt                   Hilt module: provides DB + DAO
├── model/
│   ├── local/
│   │   ├── entity/TodoEntity.kt            @Entity — Room table definition
│   │   ├── dao/TodoDao.kt                  @Dao — queries and mutations
│   │   └── TodoDatabase.kt                 @Database — RoomDatabase
│   └── repository/
│       └── TodoRepository.kt               Single source of truth
└── presentation/
    ├── navigation/
    │   ├── Screen.kt                       Sealed class — type-safe routes
    │   └── NavGraph.kt                     NavHost wiring
    ├── ui/screens/
    │   ├── TodoListScreen.kt               List + checkbox + delete + FAB
    │   └── AddTodoScreen.kt                Text field + save button
    └── view_model/
        ├── list/                           ViewModel + UiState + NavigationEvent
        └── add/                            ViewModel + UiState + NavigationEvent
```
