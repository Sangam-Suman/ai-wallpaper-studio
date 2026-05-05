package com.example.aiwallpaper.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.aiwallpaper.data.model.WallpaperStyle
import com.example.aiwallpaper.storage.AppContainer
import com.example.aiwallpaper.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onBack: () -> Unit) {
    val prefs = remember { AppContainer.encryptedPrefs() }
    val vm: ScheduleViewModel = viewModel(factory = ScheduleViewModel.Factory(prefs))
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            snackbarHostState.showSnackbar("Schedule saved!")
            vm.clearSaved()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Auto Wallpaper", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Daily Generation Card
            ScheduleCard(title = "Daily Generation", subtitle = "Generate a fresh wallpaper at a set time each day") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable", color = OnSurface, modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.dailyEnabled,
                        onCheckedChange = { vm.setDailyEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonPurple, checkedTrackColor = NeonPurple.copy(alpha = 0.4f))
                    )
                }

                if (state.dailyEnabled) {
                    Spacer(Modifier.height(12.dp))
                    Text("Generate at ${state.dailyHour}:00", color = OnSurface, style = MaterialTheme.typography.bodyMedium)
                    Slider(
                        value = state.dailyHour.toFloat(),
                        onValueChange = { vm.setDailyHour(it.toInt()) },
                        valueRange = 0f..23f,
                        steps = 22,
                        colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple)
                    )

                    Spacer(Modifier.height(4.dp))
                    Text("Prompt", color = OnSurface, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = state.dailyPrompt,
                        onValueChange = { vm.setDailyPrompt(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        placeholder = { Text("Describe your daily wallpaper…", color = OnSurfaceMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPurple,
                            unfocusedBorderColor = NeonPurple.copy(alpha = 0.35f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = NeonPurple,
                            focusedContainerColor = CardBackground,
                            unfocusedContainerColor = CardBackground
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4
                    )

                    Spacer(Modifier.height(12.dp))
                    Text("Style", color = OnSurface, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        WallpaperStyle.entries.forEach { style ->
                            val selected = style == state.dailyStyle
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) NeonPurple.copy(alpha = 0.25f) else CardBackground)
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) NeonPurple else NeonPurple.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { vm.setDailyStyle(style) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    style.label,
                                    color = if (selected) NeonPurple else OnSurface,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Collection Rotation Card
            ScheduleCard(title = "Rotate Collection", subtitle = "Cycle through your saved wallpapers automatically") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Enable", color = OnSurface, modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.rotationEnabled,
                        onCheckedChange = { vm.setRotationEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = NeonPurple, checkedTrackColor = NeonPurple.copy(alpha = 0.4f))
                    )
                }

                if (state.rotationEnabled) {
                    Spacer(Modifier.height(12.dp))
                    Text("Change wallpaper every", color = OnSurface, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    val intervals = listOf(6 to "6 hours", 12 to "12 hours", 24 to "Daily", 48 to "Every 2 days")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        intervals.forEach { (hours, label) ->
                            val selected = hours == state.rotationIntervalHours
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (selected) NeonPurple.copy(alpha = 0.25f) else CardBackground)
                                    .border(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) NeonPurple else NeonPurple.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { vm.setRotationInterval(hours) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    label,
                                    color = if (selected) NeonPurple else OnSurface,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Save button
            Button(
                onClick = { vm.save(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Save Schedule", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ScheduleCard(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(DarkSurface, CardBackground)))
            .border(1.dp, NeonPurple.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(title, color = Color.White, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(subtitle, color = OnSurfaceMuted, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(14.dp))
        content()
    }
}
