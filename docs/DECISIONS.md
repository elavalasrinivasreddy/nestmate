# Nestmate — Decision Log (ADRs)

*Lightweight architecture decision records. Newest at the bottom. Each: context → decision → consequence.*

---

### ADR-001 — Native Android (not cross-platform)
**Context:** Solo personal project; Android Studio already installed; goal is to learn/build the Android stack well.
**Decision:** Build native Android. No Flutter / React Native.
**Consequence:** Android-only for now; iOS/web are a far-future consideration, not a constraint today.

### ADR-002 — Kotlin + Jetpack Compose + Material 3
**Context:** Modern Android default; declarative UI.
**Decision:** Kotlin 2.2.x, Jetpack Compose, Material 3.
**Consequence:** Compose-idiomatic, state-driven UI; no XML layouts.

### ADR-003 — Firebase as backend
**Context:** One developer needs auth, a realtime datastore, file storage, and push without running servers.
**Decision:** Firebase — Auth, Cloud Firestore, Storage (later), FCM (later).
**Consequence:** Fast to a working multi-user app; some vendor lock-in; data modeling follows Firestore's document/query constraints.

### ADR-004 — MVVM + Repository, unidirectional state
**Context:** Want testable, layered code that keeps Firebase out of the UI.
**Decision:** Composables → ViewModels (`StateFlow<UiState>`) → Repository interfaces → Firebase data sources.
**Consequence:** Clear boundaries; repositories are fakeable in tests; a little more boilerplate.

### ADR-005 — Hilt for dependency injection
**Context:** Need DI for repositories and Firebase singletons; want compile-time safety.
**Decision:** Hilt (with KSP).
**Consequence:** Standard, checked DI; adds annotation processing to the build.

### ADR-006 — Navigation Compose, single-activity
**Context:** Compose-first navigation.
**Decision:** Single-activity app with Navigation Compose and type-safe routes.
**Consequence:** All screens are composables under one `NavHost`.

### ADR-007 — Single Gradle module for v1
**Context:** Solo project; premature modularization slows early work.
**Decision:** Keep one `:app` module, organized package-by-feature.
**Consequence:** Simpler now; can extract modules later if it grows.

### ADR-008 — minSdk 24 / targetSdk 36
**Context:** Balance reach vs. modern APIs.
**Decision:** minSdk 24 (Android 7.0), targetSdk 36.
**Consequence:** Broad device coverage with current platform features.

### ADR-009 — Gradle version catalog
**Context:** AS scaffolded `gradle/libs.versions.toml`.
**Decision:** Manage all dependency versions through the catalog.
**Consequence:** One place for versions; new deps are added there first.

### ADR-010 — `google-services.json` is git-ignored
**Context:** Keep project config/keys out of version control (esp. if pushed to a public remote).
**Decision:** Ignore it in git; document re-download in `SETUP.md`.
**Consequence:** Repo is clean/portable; a fresh clone must add the file before building.

### ADR-011 — v1 scope = trust-first two-sided core; defer photos, reviews, AI
**Context:** Want a complete, working loop before adding heavier features; Storage is blocked on billing.
**Decision:** v1 = auth + profiles + vacancy & requirement CRUD + discovery + chat + bookmarks. Photos, reviews/ratings, and the AI layer come after.
**Consequence:** A shippable, demoable core first; the AI differentiator is built on top of a working marketplace.

### ADR-012 — Location filtering by city/area string in v1
**Context:** Geo-radius search needs geohashing and more complex indexes.
**Decision:** v1 filters by city/area equality (`array-contains`/equality), index-friendly.
**Consequence:** Simple and fast now; lat/lng + geohash radius search is a later enhancement.

### ADR-013 — Phone auth via Firebase test number in dev
**Context:** Real SMS is capped at 10/day until billing is added.
**Decision:** Use the registered test number (`+91 829…` / `123456`) during development.
**Consequence:** Unlimited auth testing without burning SMS; real-device testing needs the SHA-1 added.
