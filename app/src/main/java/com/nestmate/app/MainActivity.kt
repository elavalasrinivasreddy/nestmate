package com.nestmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.nestmate.app.core.navigation.NestmateNavHost
import com.nestmate.app.ui.theme.NestmateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NestmateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NestmateNavHost(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}
