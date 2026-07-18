# Nestmate — Roadmap

*Living document. Last updated: 2026-07-18. Live progress lives in `STATUS_TRACKER.md`.*

Phases are sequenced so the app is **runnable and demoable at the end of each one**. v1 = Phases 1–9 (the trust-first two-sided core). Everything after is post-v1.

---

## Phase 0 — Project & docs scaffolding
**Goal:** repo, docs, and trackers in place.
**Deliverables:** git repo, `.gitignore`, README, full `docs/` set, status/bugs/reviews trackers.
**Exit:** documentation complete; project opens and builds as the default Compose scaffold.

## Phase 1 — Foundation
**Goal:** wire the architecture so feature work is fast — with a low-risk, plugin-free Gradle sync.
**Deliverables:** add **libraries only** (Navigation Compose, Lifecycle ViewModel/Runtime-Compose, Coroutines); `NestmateApplication` + manual DI `AppContainer` (no Hilt — ADR-014); Nestmate-branded theme; navigation skeleton; `DataResult`; a branded Welcome screen.
**Exit:** app builds and runs showing the Welcome screen. *(Requires you to run a Gradle sync.)*
**Note:** Firebase + the `google-services` plugin were intentionally moved to **Phase 2** (first use) to isolate AGP-9 plugin risk (ADR-015).

## Phase 2 — Authentication ✅ Done
**Goal:** real accounts.
**Deliverables:** `google-services` plugin + Firebase BOM (Auth). **2:** phone auth (OTP) using `PhoneAuthProvider` reachable from `AuthScreen` to handle signup and sign-in directly. Auth-state gating (signed-out → Welcome/Auth, signed-in → Home), sign-out.
**Exit:** Phone authentication works, creating an account and staying signed in across restarts. Real-device SMS/test-number run is a manual step for you (needs the Firebase console **Phone** provider enabled + the SHA-1 in `SETUP.md`).

## Phase 3 — Profile ✅ Done
**Goal:** trustworthy identity.
**Deliverables:** create/edit profile (name, type, occupation, bio, preferred locations, lifestyle); show verification badges (phone verified).
**Exit:** profile persists to `users/{uid}` and is editable. Onboarding flow implemented requiring profile creation upon first sign-in.

## Phase 4 — Vacancy listings ("I have a room") ✅ Done
**Goal:** the supply side.
**Deliverables:** create/edit/delete a vacancy; listings feed; listing detail.
**Exit:** a room-holder can post, edit, delete; others can browse and open a listing. Handled composite-index requirement gracefully via copyable error UI.

## Phase 5 — Requirement listings ("I need a room") ✅ Done
**Goal:** the demand side — the two-way differentiator.
**Deliverables:** create/edit/delete a requirement; requirements feed; detail.
**Exit:** a seeker can post what they want; room-holders can browse it via the dual-feed dashboard.

## Phase 6 — Discovery ✅ Done
**Goal:** make the marketplace usable.
**Deliverables:** search + filters by location, budget range, and room type, across both listings and requirements; empty/loading states.
**Exit:** filtered queries return correct results. Filtering implemented client-side to avoid Firestore composite index explosion.

## Phase 7 — Chat ✅ Done
**Goal:** connect the two sides.
**Deliverables:** start a conversation from a listing/requirement; real-time message thread; conversation list with last-message preview.
**Exit:** two accounts can exchange messages in real time. Connected detail screens to messaging and inbox to dashboard.

## Phase 8 — Bookmarks ✅ Done
**Goal:** let users keep track.
**Deliverables:** save/unsave listings and requirements; saved-items screen.
**Exit:** bookmarks persist per user and render quickly via snapshot denormalization. Accessible from the main dashboard.

## Phase 9 — Trust hardening (v1 gate)
**Goal:** make it safe and solid before calling v1 done.
**Deliverables:** deploy & test Firestore security rules; input validation everywhere; consistent empty/error/loading states; basic report/block; profile-verification polish.
**Exit:** no open writes; rules tested; the full loop is robust. **→ v1 complete.**

---

## Post-v1

| Phase | Theme | Notes |
|---|---|---|
| 10 | **Photos** | Unblocks once Firebase Storage is enabled (billing). Upload/display/manage for listings + profiles. |
| 11 | **Reviews, ratings & reporting** | Needs usage volume to matter; moderation tooling. |
| 12 | **AI layer** *(your edge)* | Conversational search, embeddings-based compatibility matching, fake-listing/scam detection. |
| 13+ | **Transactions & expansion** | Visit scheduling, digital agreements, payments; later iOS/web. |

## Known dependencies / blockers

- **Firebase Storage** off (billing) → photos deferred to Phase 10.
- **Phone auth** uses the dev test number (+91 829…, code `123456`); real SMS is capped at 10/day until billing is added.
- **SHA-1** fingerprint generated (`SETUP.md`) but must still be added to the Firebase Android app in-console before real phone auth works on device.
