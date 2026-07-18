# Nestmate — Status Tracker

*The live view of where the build is. Update as phases move. Last updated: 2026-07-18.*

**Current focus:** Phase 2 (Auth) code-complete and build-verified (2a email/password + 2b phone verification, both compile, dex, and unit-test clean — `./gradlew assembleDebug testDebugUnitTest lintDebug` all pass). Real-device signup/phone testing and Firebase-console setup (enable providers, add SHA-1) are still yours to run. Phase 3 (Profile) is next.

## Legend
`⬜ Not started` · `🟦 In progress` · `✅ Done` · `⛔ Blocked`

## Phase status

| Phase | Name | Status | Started | Completed | Notes |
|---|---|---|---|---|---|
| 0 | Project & docs scaffolding | ✅ Done | 2026-06-15 | 2026-06-15 | Repo, .gitignore, README, full docs set, trackers. Git init pending user run of `scripts/init-git.sh`. |
| 1 | Foundation (deps, DI, theme, nav) | ✅ Done | 2026-06-15 | 2026-06-15 | Libraries only (Navigation, Lifecycle-Compose, Coroutines). Manual DI — no Hilt (ADR-014). Branded theme + Welcome screen. Code complete; first sync verifies. |
| 2 | Authentication | ✅ Done | 2026-06-15 | 2026-07-18 | 2a: google-services plugin + Firebase Auth, email/password sign-in/up, auth gating, sign-out. 2b: phone verification (`PhoneAuthProvider`), links to the signed-in account (ADR-019), reachable from Home. Both build/dex/lint clean; ViewModel logic unit-tested. Real-device signup + phone SMS flow still needs your hands. |
| 3 | Profile | ⬜ Not started | | | |
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
| 2026-06-15 | Phase 2a: email/password auth; google-services plugin + `enableLegacyVariantApi` flag (ADR-017); phone verification deferred to 2b (ADR-018). |
| 2026-07-18 | Corrected ADR-017: `android.enableLegacyVariantApi` is removed in AGP 9 and breaks the build if set — never actually needed. Phase 2b: phone verification shipped, linked to the signed-in account (ADR-019). Confirmed this sandbox can run Gradle directly (ADR-020) — build/test/lint now verify every phase before it's marked done. |
