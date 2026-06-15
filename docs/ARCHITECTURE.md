# Nestmate — Architecture

*Living document. Last updated: 2026-06-15.*

## Principles

- **Unidirectional data flow.** UI observes immutable state; events flow up, state flows down.
- **Separation of concerns.** UI knows nothing about Firebase; it talks to ViewModels, which talk to repositories, which own the data sources.
- **Single source of truth.** Repositories expose `Flow`s; the UI renders whatever the latest state is.
- **Testable boundaries.** Repositories are interfaces so they can be faked in tests.
- **Boring and solid over clever.** One module, clear packages, standard Jetpack libraries.

## High-level shape (MVVM + Repository)

```
Composable (UI)  ──events──▶  ViewModel  ──calls──▶  Repository (interface)
      ▲                          │                        │
      └──────  UiState  ◀────────┘                  Firebase data source
                (StateFlow)                     (Auth / Firestore / Storage)
```

- **UI layer** — Jetpack Compose + Material 3. Stateless composables driven by a `UiState`.
- **ViewModel** — holds screen `UiState` as `StateFlow`, handles events, calls repositories, runs in `viewModelScope`.
- **Domain models** — plain Kotlin data classes in `data/model`, independent of Firestore.
- **Repository** — interface + Firebase-backed implementation; maps Firestore docs ↔ domain models; exposes `Flow` for reads and `suspend` functions returning a `Result` for writes.
- **Data sources** — thin wrappers over Firebase Auth, Firestore, (later) Storage and FCM.

## Target package structure (`com.nestmate.app`)

```
core/
  common/         Result<T>, validators, constants, extensions, dispatchers
  designsystem/   theme (Color/Type/Theme), reusable components (buttons, fields, cards)
  navigation/     NavHost, typed route definitions, top-level nav graph
data/
  model/          User, Profile, Listing (vacancy), Requirement, Conversation, Message, Bookmark, enums
  remote/         FirebaseAuthSource, FirestoreSource (+ collection refs)
  repository/      AuthRepository, ProfileRepository, ListingRepository,
                  RequirementRepository, ChatRepository, BookmarkRepository (+ Impl)
di/               Hilt modules (FirebaseModule, RepositoryModule, DispatchersModule)
feature/
  auth/           sign-in, sign-up, phone verification + ViewModels
  profile/        view/edit profile
  listing/        list, detail, create/edit vacancies
  requirement/    list, detail, create/edit requirements
  discovery/      search + filters
  chat/           conversation list + message thread
  bookmark/       saved items
NestmateApplication.kt   (@HiltAndroidApp)
MainActivity.kt          (sets up Compose + NavHost)
```

## Key choices

- **DI: Hilt.** Standard, compile-time-checked DI. Provides Firebase singletons and repository bindings.
- **Navigation: Navigation Compose** with type-safe routes (Kotlin serialization). Single-activity app.
- **Async: Coroutines + Flow.** Firestore listeners are wrapped as `callbackFlow`; IO on an injected dispatcher.
- **State: `StateFlow<UiState>`** per screen, collected with `collectAsStateWithLifecycle()`.
- **Error handling:** repositories return `Result<T>` (a sealed `Success`/`Error`); ViewModels translate errors into user-facing `UiState` messages. No exceptions leak to the UI.
- **Immutability:** domain models and UI state are immutable `data class`es; updates create copies.

## Firebase integration points

| Concern | Firebase service | Notes |
|---|---|---|
| Auth | Firebase Auth | Email/password + Phone (test number in dev) |
| Data | Cloud Firestore | All listings, requirements, profiles, chats |
| Realtime chat | Firestore listeners | `conversations/{id}/messages` snapshot stream |
| Photos | Storage | **Deferred** until billing/Storage is enabled |
| Push | FCM | Post-v1 (new-message notifications) |

## Threading & lifecycle

- All Firebase IO on `Dispatchers.IO` (injected, swappable in tests).
- ViewModels survive config changes; Compose state is lifecycle-aware.
- Firestore snapshot listeners are tied to `viewModelScope` and cancelled automatically.

## Testing approach

- **Unit:** ViewModels with fake repositories; validators and mappers in isolation. (JUnit already wired.)
- **Repository:** against the Firestore emulator where practical.
- **UI:** Compose UI tests for critical flows (auth, create listing, chat) — added as flows stabilize.
- Target: meaningful coverage on domain/logic, not a coverage-number chase.

## Security posture

Firestore starts in test mode but **v1 is not "done" until rules are locked** (see `DATA_MODEL.md` → Security rules). Principle: users can read public listings/requirements, but can only write/edit/delete their own documents; chat readable only by its participants.
