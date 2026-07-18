package com.nestmate.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nestmate.app.NestmateApplication

/**
 * Signed-in placeholder. The real two-sided feed (listings + requirements)
 * arrives in later phases. For now it confirms auth works, offers phone
 * verification (ADR-018 — 2b), and sign-out.
 */
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    onVerifyPhone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as NestmateApplication).container }
    val user = container.authRepository.currentUser

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "You're signed in",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Signed in as ${user?.email ?: "your account"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (user?.phoneNumber != null) {
                    "Phone verified: ${user.phoneNumber}"
                } else {
                    "Phone not verified yet"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Text(
                text = "Profiles, room listings, and the two-sided feed land in the next phases.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            if (user?.phoneNumber == null) {
                Button(onClick = onVerifyPhone) {
                    Text("Verify phone number")
                }
                Spacer(Modifier.height(12.dp))
            }
            OutlinedButton(
                onClick = {
                    container.authRepository.signOut()
                    onSignOut()
                }
            ) {
                Text("Sign out")
            }
        }
    }
}
