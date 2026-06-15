package com.nestmate.app

import android.app.Application
import com.nestmate.app.core.di.AppContainer
import com.nestmate.app.core.di.DefaultAppContainer

/**
 * Application entry point.
 *
 * Holds the app-wide [AppContainer] (manual dependency injection — see
 * docs/DECISIONS.md, ADR-014). Repositories and data sources are constructed
 * in the container starting in Phase 2.
 */
class NestmateApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
