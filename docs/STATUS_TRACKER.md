# Nestmate — Status Tracker

*The live view of where the build is. Update as phases move. Last updated: 2026-07-18.*

**Current focus:** Phase 3 (Profile) is code-complete and build-verified (Firestore integration, profile creation/edit, and onboarding routing). Next is Phase 4 (Vacancy listings).

## Legend
`⬜ Not started` · `🟦 In progress` · `✅ Done` · `⛔ Blocked`

## Phase status

| Phase | Name | Status | Started | Completed | Notes |
|---|---|---|---|---|---|
| 0 | Project & docs scaffolding | ✅ Done | 2026-06-15 | 2026-06-15 | Repo, .gitignore, README, full docs set, trackers. Git init pending user run of `scripts/init-git.sh`. |
| 1 | Foundation (deps, DI, theme, nav) | ✅ Done | 2026-06-15 | 2026-06-15 | Libraries only (Navigation, Lifecycle-Compose, Coroutines). Manual DI — no Hilt (ADR-014). Branded theme + Welcome screen. Code complete; first sync verifies. |
| 2 | Authentication | ✅ Done | 2026-06-15 | 2026-07-18 | `google-services` plugin + Firebase Auth, phone auth (`PhoneAuthProvider`), auth gating, sign-out. Code/UI completely switched to use Phone Auth exclusively (ADR-021). Both build/dex/lint clean; ViewModel logic unit-tested. Real-device signup + phone SMS flow still needs your hands. |
| 3 | Profile | ✅ Done | 2026-07-18 | 2026-07-18 | Firestore `users` collection setup. Modern UI with Material 3 chips/segmented controls for enums. Home screen intercepts users without profiles for mandatory onboarding. Build verified. |
| 4 | Vacancy listings | ⬜ Not started | | | |
| 5 | Requirement listings | ⬜ Not started | | | |
| 6 | Discovery (search + filters) | ⬜ Not started | | | Will need Firestore composite indexes. |
| 7 | Chat | ⬜ Not started | | | Realtime via Firestore listeners. |
| 8 | Bookmarks | ⬜ Not started | | | |
| 9 | Trust hardening (v1 gate) | ⬜ Not started | | | Lock Firestore rules; validation; report/block. |
| 10 | Photos | ⛔ Blocked | | | Needs Firebase Storage (billing). |
| 11 | Reviews & ratings | ⬜ Not started | | | Post-v1. |
| 12 | AI layer | ⬜ Not started | | | Post-v1; the differentiator. |

## Known dependencies / blockers

| Item | Impact | Status |
|---|---|---|
| Firebase **Storage** not enabled (billing) | No photo uploads | Deferred → Phase 10 |
| **SMS quota** 10/day (no billing) | Limits real-SMS phone auth | Use test number in dev |
| **SHA-1** not yet added to Firebase | Real-device phone auto-retrieval/instant-validation won't work; reCAPTCHA-fallback flow still usable | Run `./gradlew signingReport`, add in console |
| ~~Sandbox can't reach HDD via shell~~ | — | **Resolved (ADR-020):** Android Studio's bundled JBR works as `JAVA_HOME`; Gradle builds/tests/lint run fine from here. |

## Decision / sign-off log

| Date | Note |
|---|---|
| 2026-06-15 | v1 scope locked: trust-first two-sided core. Stack: Kotlin/Compose + Firebase. |
| 2026-06-15 | Phase 1 refined: libraries-only sync; **manual DI instead of Hilt** (ADR-014); Firebase moved to Phase 2 (ADR-015). |
| 2026-06-15 | Phase 2: phone authentication setup; email auth initially setup but later removed in favor of OTP (ADR-021). |
| 2026-07-18 | Corrected ADR-017: `android.enableLegacyVariantApi` is removed in AGP 9 and breaks the build if set — never actually needed. Phase 2: phone authentication via OTP shipped (ADR-021). Confirmed this sandbox can run Gradle directly (ADR-020) — build/test/lint now verify every phase before it's marked done. |
| 2026-07-18 | Phase 3: Profile setup integrated with Firestore. Onboarding flow routes new users to profile creation automatically from the Home screen. Used `material-icons-core` instead of `extended` to prevent APK bloat. |
