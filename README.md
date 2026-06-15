# Nestmate

> Find a room. Find a roommate. Without the chaos of scattered WhatsApp groups and sketchy listings.

**Nestmate** is a trust-first, two-sided housing-discovery app for students and relocating professionals. One side posts *"I have a room"*; the other posts *"I'm looking for a room"*; the app helps them find each other and message safely — with verification and good filtering built in from day one.

**Status:** 🚧 In active development — v1 (trust-first two-sided core)
**Platform:** Android · Kotlin · Jetpack Compose
**Backend:** Firebase (Auth · Firestore · Storage · Cloud Messaging)

---

## What v1 includes

- Email + phone authentication with verified profiles
- Post / edit / delete **room vacancies** ("I have a room")
- Post / edit / delete **accommodation requirements** ("I need a room")
- Search & filter by location, budget, and room type
- In-app, real-time chat between the two sides
- Bookmarks for listings and requirements

Photo uploads (needs Firebase Storage) and AI-assisted search + fraud detection are intentionally scoped **after** v1 — see [`docs/ROADMAP.md`](docs/ROADMAP.md).

## Tech stack

| Layer | Choice |
|---|---|
| Language | Kotlin 2.2.x |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository, unidirectional state (`StateFlow`) |
| Dependency injection | Hilt |
| Navigation | Navigation Compose (type-safe routes) |
| Async | Coroutines + Flow |
| Backend | Firebase Auth, Cloud Firestore, Storage, FCM |
| Min / Target SDK | 24 / 36 |
| Build | Gradle (Kotlin DSL) + version catalog |

## Planned project structure

```
com.nestmate.app/
├─ NestmateApplication.kt      # @HiltAndroidApp
├─ MainActivity.kt
├─ core/
│  ├─ common/                  # Result types, validators, constants, extensions
│  ├─ designsystem/            # theme, reusable Compose components
│  └─ navigation/              # NavHost + typed routes
├─ data/
│  ├─ model/                   # domain models (User, Listing, Requirement, …)
│  ├─ remote/                  # Firebase data sources
│  └─ repository/              # repository interfaces + implementations
├─ di/                         # Hilt modules
└─ feature/
   ├─ auth/  · profile/  · listing/  · requirement/
   ├─ discovery/  · chat/  · bookmark/
```

The current project is a default Compose scaffold; the structure above is built out in Phase 1 (see roadmap).

## Getting started

Full setup — Firebase, SDKs, signing, running — is in [`docs/SETUP.md`](docs/SETUP.md). In short:

1. Open the project in Android Studio (it already targets Firebase project *Nestmate*).
2. Make sure `app/google-services.json` is present (it's git-ignored — see SETUP).
3. Sync Gradle, then Run on a device/emulator (API 24+).

### Initialize git (one time)

```bash
bash scripts/init-git.sh
```

## Documentation

| Doc | Purpose |
|---|---|
| [Product Spec](docs/PRODUCT_SPEC.md) | What we're building and for whom |
| [Architecture](docs/ARCHITECTURE.md) | How the app is structured |
| [Data Model](docs/DATA_MODEL.md) | Firestore collections + security rules |
| [Roadmap](docs/ROADMAP.md) | Phase-by-phase build plan |
| [Status Tracker](docs/STATUS_TRACKER.md) | Live progress, blockers, dependencies |
| [Decisions](docs/DECISIONS.md) | Architecture decision log (ADRs) |
| [Bugs](docs/BUGS.md) | Bug log |
| [Reviews](docs/REVIEWS.md) | Self-review / code-review log |

## A note on this project

Nestmate is a personal project, built for the craft of building a complete, capable app end-to-end — not to commercialize. Quality and completeness over growth.
