# Nestmate — Bug Log

*Track defects here as they're found. Newest at the top. Last updated: 2026-06-15.*

## Severity legend
- **S1 — Critical:** crash, data loss, security hole, or blocks a core flow.
- **S2 — Major:** a feature is broken or clearly wrong, but there's a workaround.
- **S3 — Minor:** small functional issue, edge case.
- **S4 — Cosmetic:** UI/polish only.

## Status values
`Open` · `In progress` · `Fixed` · `Won't fix` · `Can't reproduce`

## Open / active

| ID | Date | Sev | Area | Description | Status | Resolution / commit |
|----|------|-----|------|-------------|--------|---------------------|
| B-001 | 2026-07-18 | S3 | Profile | `ProfileViewModel` uses `addSnapshotListener` for `loadProfile`. If the cache syncs or a remote update happens while the user is typing in the edit form, it will overwrite their unsaved UI state. Should use a single `get()` for edit flows. | Open | — |
| —  | —    | —   | —    | *No bugs logged yet.* | — | — |

## Resolved

| ID | Date | Sev | Area | Description | Resolution / commit |
|----|------|-----|------|-------------|---------------------|
| —  | —    | —   | —    | *None yet.* | — |

---

### How to log a bug
Add a row to **Open / active** with the next `B-###` id (B-001, B-002, …), today's date, a severity, the feature area (auth, listing, chat, …), and a one-line description with steps to reproduce. Move it to **Resolved** with the fixing commit when done.
