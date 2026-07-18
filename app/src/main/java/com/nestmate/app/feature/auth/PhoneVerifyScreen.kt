package com.nestmate.app.feature.auth

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nestmate.app.NestmateApplication

/**
 * Phone verification (ADR-018 — 2b). Enter a phone number, receive/enter an
 * SMS code, and the number gets linked as a verified factor on the current
 * account. In dev, use the Firebase test number from `docs/SETUP.md`.
 */
@Composable
fun PhoneVerifyScreen(
    onVerified: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PhoneVerificationViewModel = viewModel(
        factory = PhoneVerificationViewModel.provideFactory(
            (LocalContext.current.applicationContext as NestmateApplication).container.authRepository
        )
    )
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalContext.current as Activity

    LaunchedEffect(state.isVerified) {
        if (state.isVerified) onVerified()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Verify your phone",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Adds a verified badge to your profile.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            when (state.step) {
                PhoneVerificationViewModel.Step.ENTER_PHONE -> {
                    OutlinedTextField(
                        value = state.phoneNumber,
                        onValueChange = viewModel::onPhoneNumberChange,
                        label = { Text("Phone number") },
                        placeholder = { Text("+91XXXXXXXXXX") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        supportingText = { Text("Include the country code, e.g. +91") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.sendCode(activity) },
                        enabled = state.canSendCode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Send code")
                        }
                    }
                }

                PhoneVerificationViewModel.Step.ENTER_CODE -> {
                    Text(
                        text = "Enter the code sent to ${state.phoneNumber.trim()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.code,
                        onValueChange = viewModel::onCodeChange,
                        label = { Text("Verification code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))
                    Button(
                        onClick = viewModel::confirmCode,
                        enabled = state.canConfirmCode,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Verify")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { viewModel.sendCode(activity) }) {
                        Text("Resend code")
                    }
                }
            }

            if (state.errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
