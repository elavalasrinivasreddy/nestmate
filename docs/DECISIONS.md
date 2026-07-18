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
**Status:** ⚠️ Superseded by ADR-014 — deferred due to AGP 9 incompatibility.

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

### ADR-014 — Defer Hilt; use manual DI for now (supersedes ADR-005)
**Context:** Hilt's Gradle plugin has known incompatibilities with AGP 9's removal of the legacy variant API, and this project can't be compile-tested in the build environment — Hilt is the riskiest piece for a clean first build.
**Decision:** Use lightweight **manual DI** — an `AppContainer` held by `NestmateApplication` — instead of Hilt. Revisit once Hilt's AGP 9 support is confirmed stable.
**Consequence:** Zero annotation-processing/plugin risk. The architecture (repositories behind interfaces) is unchanged, so a later migration to Hilt is mechanical.

### ADR-015 — Add Firebase + google-services plugin in Phase 2, not Phase 1
**Context:** The google-services Gradle plugin is the other AGP-9-sensitive integration, and nothing in Phase 1 actually uses Firebase yet.
**Decision:** Phase 1 adds **libraries only** (Navigation, Lifecycle-Compose, Coroutines). The `google-services` plugin and Firebase BOM are added at the start of Phase 2 (Auth).
**Consequence:** Phase 1 gets a low-risk, plugin-free Gradle sync; any AGP-9/Firebase plugin issues are isolated to Phase 2.

### ADR-016 — Rely on AGP 9 built-in Kotlin (no `kotlin-android` plugin)
**Context:** AGP 9.2 compiles Kotlin natively; the scaffold has no separate `kotlin-android` plugin.
**Decision:** Use AGP 9's built-in Kotlin; don't add the `kotlin-android` plugin.
**Consequence:** Simpler plugin set; note that older guides assume a separate Kotlin plugin.

### ADR-017 — `android.enableLegacyVariantApi` not needed on AGP 9.2 (superseded)
**Context:** Early concern that AGP 9's removal of the legacy variant API would break the google-services plugin, so this ADR originally set `android.enableLegacyVariantApi=true` in `gradle.properties` as a precaution.
**Correction (2026-07-18):** Verified against the actual build (AGP 9.2.1, google-services 4.4.2): the property was **removed in AGP 9.0** and now causes a hard `EvalIssueException` ("has no effect, use android.newDsl instead") if set at all. The google-services plugin applies and processes `google-services.json` cleanly on AGP 9.2.1 with no flag needed — confirmed by a successful `compileDebugKotlin` and a built debug APK containing the Firebase Auth code.
**Decision:** Do **not** set `android.enableLegacyVariantApi` in `gradle.properties`.
**Consequence:** One fewer moving part; the earlier "temporary escape hatch" was never actually required for this AGP/plugin combination.

### ADR-018 — Email/password auth first; phone verification as a follow-up (2b)
**Context:** Phone auth (PhoneAuthProvider) needs an on-device SHA-1, a real device, and more callback wiring; email/password is fully testable immediately.
**Decision:** Ship email/password sign-in/up first in Phase 2; add phone verification as a Phase 2b sub-step once the SHA-1 is registered.
**Consequence:** A working, testable auth loop now; phone verification layers on without rework.

### ADR-019 — Phone verification links to the signed-in account; no separate phone-only sign-in
**Context:** `DATA_MODEL.md` treats `phoneNumber` as set *after* verification on an existing user, and `PRODUCT_SPEC.md` frames phone as a trust signal ("profile with verification flags"), not a second login method.
**Decision:** Phone verification (2b) is reached from Home once already signed in via email/password. `AuthRepository.startPhoneVerification`/`confirmPhoneCode` call `FirebaseUser.linkWithCredential` against `auth.currentUser` (falling back to `signInWithCredential` only if nobody is signed in, so the repository still degrades sanely if reused standalone later). No new Firestore writes yet — `phoneNumber`/`isEmailVerified` continue to come straight off `FirebaseUser` via `AuthUser`; the `users/{uid}.verification` map lands with Phase 3 (Profile).
**Consequence:** One account, two verified factors, zero rework needed when Phase 3 starts writing profile docs. Firebase's phone API is Task/callback-based (`OnVerificationStateChangedCallbacks`, `Task.addOnCompleteListener`) with no coroutine equivalent, so `AuthRepository`'s phone methods are callback-based rather than `suspend`, unlike the email methods — documented on the interface so it doesn't read as an inconsistency.

### ADR-020 — This sandbox *can* run Gradle (corrects ADR-013/STATUS_TRACKER's "sandbox can't reach HDD")
**Context:** Earlier docs assumed the agent's sandbox had no JDK and could not build on this drive, so `SETUP.md` told the user to run `./gradlew signingReport` and syncs themselves.
**Discovery (2026-07-18):** Android Studio's bundled JBR at `/snap/android-studio/232/jbr` is a full JDK 21 usable as `JAVA_HOME`. With it, `./gradlew --no-daemon <task>` builds successfully on this checkout (`local.properties` already points `sdk.dir` at a valid SDK). Verified: `compileDebugKotlin`, `assembleDebug` (produces a real, dexed debug APK), `testDebugUnitTest`, and `lintDebug` all pass end-to-end for Phase 2a + 2b.
**Decision:** Treat local Gradle builds/tests/lint as available going forward; use `--no-daemon` (the daemon hit a SIGBUS crash once — `hs_err_pid15593.log` — plausibly external-HDD/mmap related, not code).
**Consequence:** Future phases get real build/test verification before being marked done, not just a source-level review. Firebase console actions (enabling providers, adding the SHA-1 fingerprint, checking SMS quota) remain the user's job — no API access to the console from here.
