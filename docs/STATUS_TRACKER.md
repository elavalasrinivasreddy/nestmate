# Nestmate — Status Tracker

*The live view of where the build is. Update as phases move. Last updated: 2026-06-15.*

**Current focus:** Phase 0 complete → next is **Phase 1 (Foundation)**.

## Legend
`⬜ Not started` · `🟦 In progress` · `✅ Done` · `⛔ Blocked`

## Phase status

| Phase | Name | Status | Started | Completed | Notes |
|---|---|---|---|---|---|
| 0 | Project & docs scaffolding | ✅ Done | 2026-06-15 | 2026-06-15 | Repo, .gitignore, README, full docs set, trackers. Git init pending user run of `scripts/init-git.sh`. |
| 1 | Foundation (deps, DI, theme, nav) | ⬜ Not started | | | Adds Firebase + Hilt + Navigation; needs a Gradle sync. |
| 2 | Authentication | ⬜ Not started | | | Email/password + phone (test number). |
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
| **SHA-1** not yet added to Firebase | Phone auth won't work on real device | Run `./gradlew signingReport`, add in console |
| Sandbox can't reach HDD via shell | Claude runs git/Gradle through you | Use provided scripts / IDE Sync |

## Decision / sign-off log

| Date | Note |
|---|---|
| 2026-06-15 | v1 scope locked: trust-first two-sided core. Stack: Kotlin/Compose + Firebase + Hilt. |
