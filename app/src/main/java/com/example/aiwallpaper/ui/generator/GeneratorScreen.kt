package com.example.aiwallpaper.ui.generator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aiwallpaper.data.model.WallpaperStyle
import com.example.aiwallpaper.storage.AppContainer
import com.example.aiwallpaper.ui.theme.*

@Composable
fun GeneratorScreen(
    onNavigateToResult: (Long) -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val repo = remember { AppContainer.wallpaperRepository() }
    val vm: GeneratorViewModel = viewModel(factory = GeneratorViewModel.Factory(repo))
    val state by vm.state.collectAsState()
    val context = LocalContext.current

    var showKeyDialog by remember { mutableStateOf(false) }
    var newKeyInput by remember { mutableStateOf("") }
    var keyError by remember { mutableStateOf<String?>(null) }

    if (showKeyDialog) {
        AlertDialog(
            onDismissRequest = { showKeyDialog = false; newKeyInput = ""; keyError = null },
            containerColor = DarkSurface,
            title = { Text("Change API Key", color = Color.White) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newKeyInput,
                        onValueChange = { newKeyInput = it; keyError = null },
                        label = { Text("New API Key") },
                        placeholder = { Text("AIza…", color = OnSurfaceMuted) },
                        singleLine = true,
                        isError = keyError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = NeonPurple.copy(alpha = 0.4f),
                            focusedLabelColor = NeonPurple,
                            cursorColor = NeonPurple,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    if (keyError != null) {
                        Text(keyError!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newKeyInput.trim()
                        if (trimmed.length < 20) {
                            keyError = "Key looks too short — double check it"
                        } else {
                            AppContainer.apiKeyRepository().saveApiKey(trimmed)
                            showKeyDialog = false
                            newKeyInput = ""
                            keyError = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showKeyDialog = false; newKeyInput = ""; keyError = null }) {
                    Text("Cancel", color = OnSurfaceMuted)
                }
            }
        )
    }

    // Navigate to result when generation finishes
    LaunchedEffect(state.navigateToHistoryId) {
        state.navigateToHistoryId?.let { id ->
            vm.onNavigationConsumed()
            onNavigateToResult(id)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DarkBackground, DarkSurface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 52.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "AI Wallpaper",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text("Studio", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                Row {
                    IconButton(onClick = { showKeyDialog = true }) {
                        Icon(Icons.Default.Settings, "Change API Key", tint = OnSurface)
                    }
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, "History", tint = OnSurface)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Prompt input
            Text("Describe your wallpaper", color = OnSurface, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.prompt,
                onValueChange = { vm.onPromptChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                placeholder = {
                    Text(
                        "e.g. \"A dragon flying over a neon city at night\"",
                        color = OnSurfaceMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPurple,
                    unfocusedBorderColor = NeonPurple.copy(alpha = 0.35f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = NeonPurple,
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground
                ),
                shape = RoundedCornerShape(14.dp),
                maxLines = 5
            )

            if (state.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(state.error!!, color = ErrorRed, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(24.dp))

            // Style selector
            Text("Choose a style", color = OnSurface, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))

            StyleGrid(
                selected = state.selectedStyle,
                onSelect = { vm.onStyleSelected(it) }
            )

            Spacer(Modifier.height(32.dp))

            // Generate button
            Button(
                onClick = { vm.generate(context) },
                enabled = !state.isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple,
                    disabledContainerColor = NeonPurple.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (state.isGenerating) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text("Creating your wallpaper…", fontWeight = FontWeight.SemiBold)
                } else {
                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Generate Wallpaper", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            if (state.isGenerating) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "This can take 20–40 seconds…",
                    color = OnSurfaceMuted,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StyleGrid(selected: WallpaperStyle, onSelect: (WallpaperStyle) -> Unit) {
    val styles = WallpaperStyle.entries
    val styleEmojis = mapOf(
        WallpaperStyle.AMOLED to "⚫",
        WallpaperStyle.ANIME to "✨",
        WallpaperStyle.MINIMAL to "◻",
        WallpaperStyle.NATURE to "🌿"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        styles.forEach { style ->
            val isSelected = style == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) NeonPurple.copy(alpha = 0.25f) else CardBackground)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) NeonPurple else NeonPurple.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(style) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(styleEmojis[style] ?: "", fontSize = 20.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        style.label,
                        color = if (isSelected) NeonPurple else OnSurface,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
