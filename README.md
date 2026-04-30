# Todo App

A simple Todo application built with Kotlin, Jetpack Compose, Room, Hilt, and Coroutines — following the MVVM + Repository pattern taught in the Mobile Programming 2026 course.

---

## Project Architecture

### Layers at a glance

```
UI (Compose screens)
    ↕  StateFlow (data class state)
ViewModel
    ↕  suspend functions / Flow<T> / Flow<DTO>
Repository
    ↕  suspend functions / Flow<T> / Flow<DTO>
DAO
    ↕  SQLite (Room generates the implementation)
Database (Room)
```

---

### 1. Database schema

Three tables with a many-to-many relationship between `todos` and `items`:

```
todos                    todo_item_cross_ref              items
─────────────────        ────────────────────────         ────────────────────────
id          INT (PK)     todoId  INT  (FK → todos)        id           INT (PK)
title       TEXT         itemId  INT  (FK → items)        name         TEXT
isCompleted INT          [CASCADE DELETE on both]         description  TEXT
                                                          createdAt    LONG
                                                          updatedAt    LONG
```

Deleting a todo automatically removes its cross-ref rows. Deleting an item removes it from every todo it was assigned to — enforced by Room's `ForeignKey.CASCADE`.

---

### 2. Entities vs DTOs

The project uses two different kinds of model classes:

#### Entities — one per database table

Annotated with `@Entity`. Room uses these to create and migrate tables. They represent a full row.

```kotlin
@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false
)
```

#### DTOs (Data Transfer Objects) — shaped for a specific use case

Plain `data class` with **no** Room annotations. Room maps aggregate query results directly into these by matching SQL column aliases to property names. They carry only the data a specific consumer needs — nothing more.

```kotlin
// model/dto/TodoStatsDto.kt
data class TodoStatsDto(
    val totalCount: Int,
    val completedCount: Int,
    val pendingCount: Int
)

// model/dto/ItemStatsDto.kt
data class ItemStatsDto(
    val totalCount: Int,
    val assignedCount: Int   // items linked to at least one todo
)
```

Key difference: an Entity is the shape of a table row. A DTO is the shape of a query result — it can span multiple tables, contain computed columns, and omit columns that aren't needed.

---

### 3. DAO — queries and aggregate functions

**`TodoDao`** and **`ItemDao`** are interfaces. Room generates the concrete implementations at compile time via KSP.

#### Standard queries — return `Flow<Entity>`

```kotlin
// Room keeps the query "alive" — emits a new list every time the table changes
@Query("SELECT * FROM todos ORDER BY id DESC")
fun getAllTodos(): Flow<List<TodoEntity>>

// Writes run once and return
@Insert(onConflict = OnConflictStrategy.REPLACE)
suspend fun insert(todo: TodoEntity)

@Update
suspend fun update(todo: TodoEntity)

@Delete
suspend fun delete(todo: TodoEntity)
```

#### Aggregate queries — return `Flow<DTO>`

Room maps each column alias in the SELECT directly to the matching property name in the DTO.

```kotlin
// TodoDao — maps to TodoStatsDto
@Query("""
    SELECT
        COUNT(*)                                           AS totalCount,
        SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END)  AS completedCount,
        SUM(CASE WHEN isCompleted = 0 THEN 1 ELSE 0 END)  AS pendingCount
    FROM todos
""")
fun getTodoStats(): Flow<TodoStatsDto>

// ItemDao — maps to ItemStatsDto
@Query("""
    SELECT
        COUNT(*)                                                    AS totalCount,
        (SELECT COUNT(DISTINCT itemId) FROM todo_item_cross_ref)   AS assignedCount
    FROM items
""")
fun getItemStats(): Flow<ItemStatsDto>
```

Because these are `Flow`, the Stats screen updates automatically whenever any insert, update, or delete occurs on the underlying tables — no manual refresh needed.

**Threading**: Room executes all queries on an internal `Dispatchers.IO` thread pool. You never write `withContext(Dispatchers.IO)` for Room calls.

---

### 4. Repository — single source of truth

Repositories wrap DAOs and expose data upward. The ViewModel never touches a DAO directly.

```kotlin
class TodoRepository @Inject constructor(private val todoDao: TodoDao) {
    val todos: Flow<List<TodoEntity>> = todoDao.getAllTodos()
    val todoStats: Flow<TodoStatsDto>  = todoDao.getTodoStats()   // ← DTO flow

    suspend fun insert(todo: TodoEntity) = todoDao.insert(todo)
    suspend fun update(todo: TodoEntity) = todoDao.update(todo)
    suspend fun delete(todo: TodoEntity) = todoDao.delete(todo)
    fun getTodoById(todoId: Int): Flow<TodoEntity?> = todoDao.getTodoById(todoId)
}
```

```kotlin
class ItemRepository @Inject constructor(private val itemDao: ItemDao) {
    val allItems: Flow<List<ItemEntity>> = itemDao.getAllItems()
    val itemStats: Flow<ItemStatsDto>    = itemDao.getItemStats()  // ← DTO flow

    suspend fun insert(item: ItemEntity): Long = itemDao.insert(item)
    suspend fun update(item: ItemEntity) = itemDao.update(item)
    suspend fun delete(item: ItemEntity) = itemDao.delete(item)
    fun getItemsForTodo(todoId: Int): Flow<List<ItemEntity>> = itemDao.getItemsForTodo(todoId)
    suspend fun addItemToTodo(todoId: Int, itemId: Int) = ...
    suspend fun removeItemFromTodo(todoId: Int, itemId: Int) = ...
}
```

---

### 5. ViewModel — state and business logic

ViewModels own a single `MutableStateFlow<UiState>` that holds everything the screen needs to render. State is a plain `data class`, not a sealed interface — because there is no loading/error/success branching; the screen always renders whatever data is available.

```kotlin
data class TodosUiState(
    val todos: List<TodoEntity>          = emptyList(),
    val selectedTodo: TodoEntity?        = null,   // drives the bottom sheet
    val selectedTodoItems: List<ItemEntity> = emptyList(),
    val allItems: List<ItemEntity>       = emptyList(),
    val isAddTodoDialogOpen: Boolean     = false
)
```

#### Partial state updates with `update {}`

Instead of replacing the whole state on every change, `MutableStateFlow.update {}` atomically copies only the fields that changed:

```kotlin
_uiState.update { it.copy(todos = newList) }
// The other fields (allItems, selectedTodo, etc.) are preserved exactly as they were.
```

This is important when multiple coroutines update the same StateFlow concurrently — `update {}` is thread-safe and prevents one coroutine from overwriting another's changes.

#### Multiple independent collectors in `init {}`

`TodosViewModel` has three separate coroutines, each responsible for one data source:

```kotlin
init {
    // 1. Todos list — always live
    viewModelScope.launch {
        todoRepository.todos.collect { todos ->
            _uiState.update { it.copy(todos = todos,
                selectedTodo = todos.find { t -> t.id == _selectedTodoId.value }) }
        }
    }

    // 2. Items for the currently open todo — switches reactively with flatMapLatest
    viewModelScope.launch {
        _selectedTodoId
            .flatMapLatest { id ->
                if (id != null) itemRepository.getItemsForTodo(id)
                else flowOf(emptyList())
            }
            .collect { items -> _uiState.update { it.copy(selectedTodoItems = items) } }
    }

    // 3. All items — needed to populate the assignment sheet checkboxes
    viewModelScope.launch {
        itemRepository.allItems.collect { items ->
            _uiState.update { it.copy(allItems = items) }
        }
    }
}
```

#### `flatMapLatest` — switching between flows

`_selectedTodoId` is itself a `MutableStateFlow<Int?>`. When the user taps a todo, `_selectedTodoId.value` is updated. `flatMapLatest` cancels the previous inner flow (items for the old todo) and starts a new one (items for the newly selected todo). The UI always shows the correct item list without any manual refresh.

```
_selectedTodoId emits 3   →  subscribes to getItemsForTodo(3)  → emits [Item A, Item B]
_selectedTodoId emits 7   →  cancels getItemsForTodo(3)
                          →  subscribes to getItemsForTodo(7)  → emits [Item C]
_selectedTodoId emits null →  cancels getItemsForTodo(7)
                           →  subscribes to flowOf(emptyList()) → emits []
```

#### Stats ViewModel — two independent collectors, two DTOs

```kotlin
init {
    viewModelScope.launch {
        todoRepository.todoStats.collect { dto ->
            _uiState.update { it.copy(todoStats = dto) }
        }
    }
    viewModelScope.launch {
        itemRepository.itemStats.collect { dto ->
            _uiState.update { it.copy(itemStats = dto) }
        }
    }
}
```

The DTOs arrive independently. If only the item table changes, only `itemStats` emits — the todo stats are untouched.

---

### 6. Communication flow — full round-trips

#### User toggles an item's assignment to a todo

```
1.  User taps a Checkbox in the bottom sheet (UI)
2.  onToggleItem(item) callback fires
3.  TodosViewModel.onToggleItem(item) — checks if item is currently assigned
4.  viewModelScope.launch { itemRepository.addItemToTodo(todoId, item.id) }
         OR  itemRepository.removeItemFromTodo(todoId, item.id)
5.  Room executes INSERT/DELETE on todo_item_cross_ref (IO thread)
6.  Room detects the change, re-runs getItemsForTodo(todoId) query
7.  New List<ItemEntity> emitted on the Flow
8.  flatMapLatest collector receives the list (back on Main)
9.  _uiState.update { it.copy(selectedTodoItems = newList) }
10. StateFlow notifies Compose collector
11. collectAsStateWithLifecycle() triggers recomposition
12. Checkboxes re-render with correct checked state
```

#### Stats update after a todo is completed

```
1.  User taps a Checkbox on a TodoRow (UI)
2.  onToggleComplete(todo) fires
3.  TodosViewModel: repository.update(todo.copy(isCompleted = true))
4.  Room executes UPDATE on todos table (IO thread)
5a. getAllTodos() Flow emits new list   → todos collector updates TodosUiState.todos
5b. getTodoStats() Flow emits new DTO  → StatsViewModel stats collector updates StatsUiState.todoStats
    (both happen independently and concurrently)
6.  Each StateFlow notifies its own Compose collector
7.  TodosScreen re-renders the list (strikethrough applied)
8.  StatsScreen re-renders the progress bar and percentages
```

Both screens react to the same underlying database change with no shared state between their ViewModels — the database is the single source of truth.

---

### 7. UI layer — flat bottom navigation

No deep navigation stack. Three permanent tabs managed by a single `var selectedTab` integer in `MainScreen`. ViewModels are activity-scoped (obtained via `hiltViewModel()` at the `MainScreen` level) and survive tab switches.

```
MainScreen
├── NavigationBar
│   ├── 🏠 Todos (tab 0)
│   ├── ⭐ Items  (tab 1)
│   └── ℹ️ Stats  (tab 2)
│
├── Tab 0 — TodosScreen
│   ├── LazyColumn of TodoRow composables
│   ├── FAB → AlertDialog (add todo)
│   └── tap a row → ModalBottomSheet
│           ├── Todo title header
│           └── LazyColumn of all items with Checkboxes
│               checked = assigned to this todo (live via flatMapLatest)
│
├── Tab 1 — ItemsScreen
│   ├── LazyColumn of ItemRow composables
│   └── FAB / Edit button → AlertDialog (add or edit item)
│
└── Tab 2 — StatsScreen
    └── LazyColumn
        ├── item { SectionHeader("Todos") }
        ├── item { StatGroupCard — totalCount / completedCount / pendingCount / progress bar }
        ├── item { SectionHeader("Items") }
        └── item { StatGroupCard — totalCount / assignedCount / unassigned / progress bar }
```

**LazyColumn for rows, Column for card internals**: `LazyColumn` with `items {}` is used for all scrollable lists (todo rows, item rows, stat rows). The layout *inside* a card uses a regular `Column` — nesting a lazy composable inside another lazy composable is not allowed in Compose.

---

### 8. Dependency injection (Hilt)

```
@HiltAndroidApp TodoApp
    │
    └── DatabaseModule  (@Singleton)
            ├── provides TodoDatabase   (one SQLite connection for the whole app)
            ├── provides TodoDao        (@Singleton — thin proxy, safe to share)
            └── provides ItemDao        (@Singleton)
                    │
                    ├── TodoRepository  (@Inject constructor — Hilt builds it automatically)
                    │       consumed by: TodosViewModel, StatsViewModel
                    │
                    └── ItemRepository  (@Inject constructor)
                            consumed by: TodosViewModel, ItemsViewModel, StatsViewModel
```

All three ViewModels are `@HiltViewModel`. Hilt injects the repositories automatically when Compose calls `hiltViewModel<XyzViewModel>()`.

---

## Tech stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose + Material 3 |
| State management | StateFlow + `update {}` (Coroutines) |
| Dependency injection | Hilt |
| Database | Room |
| Async | Kotlin Coroutines (`viewModelScope`, `flatMapLatest`, `combine`) |
| Annotation processing | KSP |

---

## Package structure

```
com.example.todo/
├── TodoApp.kt                                    @HiltAndroidApp
├── MainActivity.kt                               @AndroidEntryPoint
├── di/
│   └── DatabaseModule.kt                         provides DB, TodoDao, ItemDao
├── model/
│   ├── dto/
│   │   ├── TodoStatsDto.kt                       aggregate query result (completed/pending counts)
│   │   └── ItemStatsDto.kt                       aggregate query result (total/assigned counts)
│   ├── local/
│   │   ├── entity/
│   │   │   ├── TodoEntity.kt                     @Entity — todos table
│   │   │   ├── ItemEntity.kt                     @Entity — items table
│   │   │   ├── TodoItemCrossRef.kt               @Entity — junction table (many-to-many)
│   │   │   └── TodoWithItems.kt                  @Relation POJO (unused in UI, available for queries)
│   │   ├── dao/
│   │   │   ├── TodoDao.kt                        CRUD + getTodoStats() aggregate
│   │   │   └── ItemDao.kt                        CRUD + getItemStats() aggregate
│   │   └── TodoDatabase.kt                       @Database version 2
│   └── repository/
│       ├── TodoRepository.kt                     exposes todos + todoStats flows
│       └── ItemRepository.kt                     exposes allItems + itemStats flows
└── presentation/
    ├── ui/
    │   ├── MainScreen.kt                         bottom nav host (3 tabs)
    │   └── screens/
    │       ├── TodosScreen.kt                    todos list + bottom sheet assignment
    │       ├── ItemsScreen.kt                    items CRUD with dialogs
    │       └── StatsScreen.kt                    LazyColumn of aggregate stat cards
    └── view_model/
        ├── todos/
        │   ├── TodosUiState.kt                   data class — todos + sheet + dialog state
        │   └── TodosViewModel.kt                 3 collectors, flatMapLatest for item assignment
        ├── items/
        │   ├── ItemsUiState.kt                   data class — items + dialog state
        │   └── ItemsViewModel.kt                 CRUD + dialog open/close
        └── stats/
            ├── StatsUiState.kt                   data class — TodoStatsDto? + ItemStatsDto?
            └── StatsViewModel.kt                 2 DTO collectors
```
