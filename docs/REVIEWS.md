# Nestmate — Review Log

*Self-review / code-review notes per phase. Keeps quality honest and tech debt visible. Last updated: 2026-06-15.*

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

---

### Tech-debt backlog
*Things consciously deferred — revisit before calling v1 done.*

1. **Theme package** lives in `com.nestmate.app.ui.theme` (AS default) rather than `core/designsystem` as in `ARCHITECTURE.md`. Consolidate when convenient.
2. **`AppContainer`** is an empty placeholder until Phase 2 adds the first repository.
3. **Hilt migration** deferred (ADR-014) — revisit once AGP 9 support is confirmed stable.
