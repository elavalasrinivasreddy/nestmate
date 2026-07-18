# Nestmate — Review Log

*Self-review / code-review notes per phase. Keeps quality honest and tech debt visible. Last updated: 2026-07-18.*

## Review checklist (run at the end of each phase)

**Architecture**
- [ ] UI has no direct Firebase calls (goes through ViewModel → Repository).
- [ ] State is immutable; single source of truth per screen.
- [ ] New code lands in the right package (feature vs core vs data).

**Correctness**
- [ ] Loading, empty, and error states handled for every async screen.
- [ ] Inputs validated before write; sensible defaults.
- [ ] No hard-coded strings/dimens that should be resources/theme.

**Security & data**
- [ ] Firestore writes constrained to the owner; rules updated if the model changed.
- [ ] No secrets committed; `google-services.json` still git-ignored.
- [ ] Only necessary fields read/written; queries have required indexes.

**Performance**
- [ ] No work on the main thread; Firestore listeners scoped & cancelled.
- [ ] Lists keyed; recomposition kept reasonable.

**Tests**
- [ ] ViewModel / validator / mapper logic covered for the phase.

**UX**
- [ ] Flow works on a real device; back navigation sane; no dead ends.

---

## Review entries

| Date | Phase / scope | Findings | Actions | Tech debt noted |
|------|---------------|----------|---------|-----------------|
| 2026-06-15 | Phase 0 — scaffolding & docs | Baseline only; default Compose scaffold + docs. Nothing to review functionally yet. | Proceed to Phase 1. | None. |
| 2026-06-15 | Phase 1 — foundation | Libraries-only Gradle change (no plugins) to de-risk the first sync on AGP 9; manual DI instead of Hilt; theme kept in `ui.theme` package; can't compile-test in this environment. | Hand off for first Sync + Run; fix any version issues iteratively. | See backlog items 1–3 below. |
| 2026-07-18 | Phase 2 — authentication (2a + 2b) | UI has no direct Firebase calls (all through `AuthRepository`); loading/error states present on both `AuthScreen` and `PhoneVerifyScreen`; password length + phone-format/code-length validated client-side before write. First time this codebase was actually build-verified — `assembleDebug`, `testDebugUnitTest`, `lintDebug` all pass. Found and fixed a real bug: `gradle.properties` was missing the `enableLegacyVariantApi` flag ADR-017 claimed was set — turned out the flag is deprecated/removed on AGP 9.2.1 and *breaks* the build if added (ADR-017 corrected). | Added `PhoneVerificationViewModelTest` (5 cases: validation gating, code-sent transition, auto-verify, confirm success/failure) via a fake `AuthRepository`. Verified sandbox can run Gradle (ADR-020). | `AuthViewModel` (2a) still has no unit tests — same gap now closed for 2b, not backfilled for 2a. Home screen still shows only email/phone, no `users/{uid}` Firestore doc yet — arrives with Phase 3. |
| 2026-07-18 | Phase 3 — Profile | Successfully integrated `firebase-firestore` and modeled `UserProfile`. UI utilizes M3 `FilterChip` and `InputChip` for intuitive enum selection without bloated `DropdownMenu`s. `HomeScreen` correctly monitors `ProfileRepository` state to enforce a mandatory "Create Profile" flow for newly authenticated users. Advisory optimization applied: substituted `material-icons-extended` with `material-icons-core` to maintain a lean APK profile while providing necessary icons (`CheckCircle`, `Add`, `Close`). | N/A | None. |
| 2026-07-18 | Phase 4 — Vacancy Listings | Created `Listing` data models and `FirestoreListingRepository` matching the data specification. Built three distinct UI layers: Feed (`ListingFeedScreen`), Details (`ListingDetailScreen`), and Form (`CreateListingScreen`), maintaining standard unidirectional data flows throughout. Surfaced Firestore `FAILED_PRECONDITION` index creation error URL clearly in a `SelectionContainer` on the Feed so developers can copy the one-time link. Reverted previous regression by ensuring sign-out remains available on the Dashboard view. Fixed currency default issue: changed "USD" -> "INR". | N/A | Missing `ProfileViewModel` and `HomeViewModel` tests from Phase 3 (logged). Missing `Listing` viewmodel tests. |
| 2026-07-18 | Phase 5 — Requirement Listings | Scaled the supply-side architecture patterns to the demand side seamlessly. Introduced a `NavigationBar` on the `HomeScreen` to house "Rooms", "Roommates", and "Profile" tabs. Continued use of `SelectionContainer` around Firestore index errors to maintain developer ergonomics. | N/A | Missing `Requirement` viewmodel tests. |
| 2026-07-18 | Phase 6 — Discovery (Filters) | Added robust filtering via `ModalBottomSheet` and `FilterChip`s. Implemented **client-side filtering** inside the `ViewModel` using Kotlin Flow's `combine` operator instead of dynamic server-side querying. This prevents developers from having to configure ~14 different composite indexes for optional filter permutations, making the MVP dramatically easier to deploy and test. | N/A | Missing viewmodel tests for filtering logic. |
| 2026-07-18 | Phase 7 — Chat | Successfully set up `conversations` and nested `messages` subcollections in Firestore. Utilized `callbackFlow` to listen for snapshot updates, creating a smooth, real-time messaging experience. Addressed composite index error directly on the inbox UI by wrapping `errorMessage` in `SelectionContainer` ensuring actionable developer logs. Replaced deprecated `Icons.Filled.Send` with `AutoMirrored` counterpart for forward-compatibility. | N/A | Missing chat viewmodel tests. |
| 2026-07-18 | Phase 8 — Bookmarks | Utilized data denormalization (saving a `snapshot` of the listing/requirement directly in the bookmark document) to bypass the N+1 query problem, making the `BookmarkListScreen` incredibly fast and reducing Firestore reads. NavHost correctly intercepts the specific itemType to route backwards into existing Detail screens. | N/A | Missing bookmark viewmodel tests. |
| 2026-07-18 | Phase 9 — Trust Hardening | Hardened Firestore reads and writes directly via `firestore.rules`. Established the `TrustRepository` and `Report` modeling. Intercepted all critical feeds (`ListingFeedViewModel`, `RequirementFeedViewModel`, `ConversationListViewModel`) by combining their streams with the current user's profile to execute seamless, client-side omission of blocked UIDs. The Nestmate v1 core is structurally complete. | N/A | Missing viewmodel tests for filtering blocked content. |

---

### Tech-debt backlog
*Things consciously deferred — revisit before calling v1 done.*

1. **Theme package** lives in `com.nestmate.app.ui.theme` (AS default) rather than `core/designsystem` as in `ARCHITECTURE.md`. Consolidate when convenient.
2. ~~`AppContainer` is an empty placeholder~~ — resolved: holds `authRepository` since Phase 2a.
3. **Hilt migration** deferred (ADR-014) — revisit once AGP 9 support is confirmed stable.
4. **`AuthViewModel` (2a) has no unit tests** — `PhoneVerificationViewModel` (2b) does. Backfill when touching auth again.
