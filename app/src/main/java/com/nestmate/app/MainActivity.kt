package com.nestmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nestmate.app.core.navigation.NestmateNavHost
import com.nestmate.app.ui.theme.NestmateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val settings = (application as NestmateApplication).container.settingsRepository
        setContent {
            val themeMode by settings.themeMode.collectAsStateWithLifecycle()
            NestmateTheme(themeMode = themeMode) {
                // Single Scaffold ownership: each destination handles its own
                // window insets (its Scaffold, or systemBarsPadding for the
                // Scaffold-less Welcome/Auth screens). No outer Scaffold here —
                // wrapping the NavHost in one double-applied the status-bar inset.
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NestmateNavHost()
                }
            }
        }
    }
}
