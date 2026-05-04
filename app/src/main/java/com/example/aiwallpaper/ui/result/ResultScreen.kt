package com.example.aiwallpaper.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aiwallpaper.storage.AppContainer
import com.example.aiwallpaper.ui.theme.*
import com.example.aiwallpaper.utils.toFormattedDate
import java.io.File

@Composable
fun ResultScreen(
    historyId: Long,
    onBack: () -> Unit,
    onRegenerate: () -> Unit
) {
    val repo = remember { AppContainer.wallpaperRepository() }
    val vm: ResultViewModel = viewModel(factory = ResultViewModel.Factory(repo))
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(historyId) { vm.load(historyId) }

    // Show one-shot toast via snackbar
    LaunchedEffect(state.toast) {
        state.toast?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearToast()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = NeonPurple
                )
                return@Box
            }

            val history = state.history
            if (history == null) {
                Text(
                    "Image not found",
                    modifier = Modifier.align(Alignment.Center),
                    color = OnSurfaceMuted
                )
                return@Box
            }

            Column(modifier = Modifier.fillMaxSize()) {

                // Wallpaper image — takes most of the screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(File(history.imagePath))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Generated wallpaper",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Top gradient overlay for back button readability
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)))
                    )

                    // Back button
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 40.dp, start = 8.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }

                // Bottom action panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        history.prompt,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${history.style}  ·  ${history.createdAt.toFormattedDate()}",
                        color = OnSurfaceMuted,
                        style = MaterialTheme.typography.labelSmall
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Download
                        OutlinedButton(
                            onClick = { vm.downloadToGallery(context) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonPurple)
                        ) {
                            Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Save", fontWeight = FontWeight.SemiBold)
                        }

                        // Set as Wallpaper
                        Button(
                            onClick = { vm.setAsWallpaper(context) },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Wallpaper, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Set", fontWeight = FontWeight.Bold)
                        }

                        // Regenerate
                        OutlinedButton(
                            onClick = onRegenerate,
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonBlue)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Redo", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
