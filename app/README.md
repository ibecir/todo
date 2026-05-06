✦ In our current Feature-Based Clean Architecture, data is organized into three distinct layers to ensure that business logic remains independent of the database or network
implementation.

Here is the breakdown of the layers and the flow for your two examples:

  ---

The Three Layers

1. Data Layer (data/): The "Source". It contains technical models like Entities (Room) and DTOs (Network). It knows how to talk to the database or the internet.
2. Domain Layer (domain/): The "Truth". It contains pure Kotlin models that represent the core of your app. It has no idea if the data came from a cloud or a local file.
3. Presentation Layer (ui/): The "Face". It contains the UI (Compose) and ViewModels. It only speaks "Domain Language" to stay decoupled from the technical details of the data
   layer.

  ---

Example 1: Local Data Flow (TodoEntity)

This is the path for data stored in your local Room database.

1. Storage (TodoDatabase): Room stores a row in the todos table matching the structure of TodoEntity.
2. Access (TodoDao): A query returns a reactive stream: Flow<List<TodoEntity>>.
3. Mapping (data/mapper/Mappers.kt): The system uses the TodoEntity.toDomain() function. This strips away Room-specific metadata (like @Entity or foreign key constraints) and
   creates a pure domain.model.Todo.
4. Abstraction (TodoRepositoryImpl): The repository collects the entities, maps them to domain models, and exposes Flow<List<Todo>>.
5. Usage (TodosViewModel): The ViewModel observes the List<Todo>. If the user toggles a checkbox, the ViewModel sends the Todo back. The Repository maps it back to a TodoEntity
   to save it.

Why this matters: If you decide to rename your database table from todos to tasks, you only change the TodoEntity. Your ViewModel and UI never break because they only know the
Todo domain model.

  ---

Example 2: Remote Data Flow (TagDto)

This is the path for data fetched from your remote API.

1. Network (TagsApi): Your server sends a JSON response. Retrofit uses kotlinx.serialization to parse this JSON into the TagDto class.
2. Mapping (data/mapper/Mappers.kt): The system uses TagDto.toDomain(). This handles the conversion from network-specific naming (like user_id in JSON) to standard Kotlin naming
   (userId in the Tag model).
3. Refinement (TagRepositoryImpl): The repository implementation fetches the DTOs from the API, filters them by the current userId, and converts the results into a list of domain
   Tag objects.
4. Presentation (TodosViewModel): The ViewModel receives a List<Tag>. It combines these tags with the Todo list to show which tags belong to which todo.
5. Rendering (TodosScreen): The UI takes the list of Tag domain objects and renders them as interactive chips.

Why this matters: If the API team changes the JSON field from user_id to owner_id, you only update the @SerialName in TagDto. The rest of your application remains untouched and
functional.

  ---

Summary Table

┌──────────────┬──────────────────────┬─────────────────────┬───────────────────────────────────────────────┐
│ Layer        │ Component            │ Model Used          │ Responsibility                                │
├──────────────┼──────────────────────┼─────────────────────┼───────────────────────────────────────────────┤
│ Data         │ DAO / API            │ TodoEntity / TagDto │ Talking to DB / Network.                      │
│ Data         │ Mapper               │ Entity/Dto → Model  │ Converting technical types to business types. │
│ Domain       │ Repository Interface │ Todo / Tag          │ Defining the contract for data access.        │
│ Presentation │ ViewModel            │ Todo / Tag          │ Managing state and business logic.            │
│ Presentation │ Compose Screen       │ Todo / Tag          │ Drawing pixels on the screen.                 │
└──────────────┴──────────────────────┴─────────────────────┴───────────────────────────────────────────────┘