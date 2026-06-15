package com.nestmate.app.core.di

/**
 * Manual dependency container (no Hilt for now — see docs/DECISIONS.md, ADR-014).
 *
 * Dependencies are added here as features land:
 *   - Phase 2: FirebaseAuth wiring + AuthRepository
 *   - Phase 3+: Profile / Listing / Requirement / Chat / Bookmark repositories
 */
interface AppContainer {
    // Intentionally empty until Phase 2 introduces the first repository.
}

class DefaultAppContainer : AppContainer {
    // Construct Firebase-backed dependencies here starting Phase 2.
}
