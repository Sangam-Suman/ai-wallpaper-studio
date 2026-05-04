package com.example.aiwallpaper.ui.onboarding

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aiwallpaper.storage.AppContainer
import com.example.aiwallpaper.ui.theme.*

@Composable
fun OnboardingScreen(onSetupComplete: () -> Unit) {
    val vm: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(AppContainer.apiKeyRepository())
    )
    val state by vm.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, DarkSurface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // App logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(NeonPurple.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🎨", fontSize = 38.sp)
            }

            Spacer(Modifier.height(20.dp))

            // Step dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .height(4.dp)
                            .width(if (index == state.step) 28.dp else 12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (index <= state.step) NeonPurple
                                else NeonPurple.copy(alpha = 0.28f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(36.dp))

            AnimatedContent(targetState = state.step, label = "step") { step ->
                when (step) {
                    0 -> WelcomeStep(onNext = { vm.nextStep() })
                    1 -> GetKeyStep(
                        onOpenLink = { context ->
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://aistudio.google.com/app/apikey")
                            )
                            context.startActivity(intent)
                        },
                        onNext = { vm.nextStep() }
                    )
                    else -> EnterKeyStep(
                        keyInput = state.keyInput,
                        isConnecting = state.isValidating,
                        error = state.error,
                        onKeyChanged = { vm.onKeyInputChanged(it) },
                        onConnect = { vm.connectAI(onSetupComplete) }
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "AI Wallpaper Studio",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Describe any wallpaper you can imagine and watch AI create it for you in seconds.",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        val features = listOf(
            "Type a description — the AI does the rest",
            "Choose AMOLED, Anime, Minimal, or Nature styles",
            "Save to gallery or set as wallpaper instantly",
            "Full history stored privately on your device"
        )
        features.forEach { feature ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(NeonPurple)
                )
                Spacer(Modifier.width(14.dp))
                Text(feature, color = OnSurface, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(40.dp))
        PrimaryButton("Get Started →", onClick = onNext)
    }
}

@Composable
private fun GetKeyStep(
    onOpenLink: (android.content.Context) -> Unit,
    onNext: () -> Unit
) {
    val context = LocalContext.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Connect Your AI Account",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "This app uses Google Gemini to generate your wallpapers. You need a free personal connection key — no credit card required to start.",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("How to get your key:", color = NeonPurple, fontWeight = FontWeight.SemiBold)
                val steps = listOf(
                    "Open Google AI Studio (button below)",
                    "Sign in with any Google account",
                    "Tap 'Create API Key' → copy it",
                    "Come back here and paste it"
                )
                steps.forEachIndexed { i, step ->
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(NeonPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("${i + 1}", color = NeonPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(step, color = OnSurface, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        OutlinedButton(
            onClick = { onOpenLink(context) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            border = BorderStroke(1.dp, NeonPurple),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Open Google AI Studio", color = NeonPurple, fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(12.dp))
        PrimaryButton("I Have My Key →", onClick = onNext)
    }
}

@Composable
private fun EnterKeyStep(
    keyInput: String,
    isConnecting: Boolean,
    error: String?,
    onKeyChanged: (String) -> Unit,
    onConnect: () -> Unit
) {
    var showKey by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Paste Your Key",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Your key stays on this device only and is never shared with anyone — not even us.",
            style = MaterialTheme.typography.bodyLarge,
            color = OnSurfaceMuted,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))

        OutlinedTextField(
            value = keyInput,
            onValueChange = onKeyChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your Connection Key") },
            placeholder = { Text("AIza…", color = OnSurfaceMuted) },
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showKey = !showKey }) {
                    Icon(
                        imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showKey) "Hide key" else "Show key",
                        tint = NeonPurple
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                unfocusedBorderColor = NeonPurple.copy(alpha = 0.4f),
                focusedLabelColor = NeonPurple,
                cursorColor = NeonPurple,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            isError = error != null,
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        if (error != null) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lock, null, tint = OnSurfaceMuted, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(6.dp))
            Text("Encrypted and stored locally", color = OnSurfaceMuted, style = MaterialTheme.typography.labelSmall)
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onConnect,
            enabled = keyInput.isNotBlank() && !isConnecting,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isConnecting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Connect AI", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun PrimaryButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
