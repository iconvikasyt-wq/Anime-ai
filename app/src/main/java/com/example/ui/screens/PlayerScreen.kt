package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AnimeSceneCanvas
import com.example.ui.theme.AnimeBorderGlow
import com.example.ui.theme.AnimeCardElevated
import com.example.ui.theme.AnimeCardSurface
import com.example.ui.theme.AnimeDarkBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.video.Mp4VideoExporter
import com.example.viewmodel.AnimeStudioViewModel
import java.io.File

@Composable
fun PlayerScreen(
    viewModel: AnimeStudioViewModel,
    modifier: Modifier = Modifier
) {
    val scenes by viewModel.activeScenes.collectAsStateWithLifecycle()
    val characters by viewModel.activeCharacters.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()

    val isPlaying by viewModel.isPlaying.collectAsStateWithLifecycle()
    val currentSceneIndex by viewModel.currentSceneIndex.collectAsStateWithLifecycle()
    val sceneProgress by viewModel.sceneProgress.collectAsStateWithLifecycle()
    val exportState by viewModel.exportState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var selectedResolution by remember { mutableStateOf(Mp4VideoExporter.Resolution.HD_720P) }

    val currentScene = scenes.getOrNull(currentSceneIndex)
    val currentCharacter = currentScene?.let { scene ->
        characters.firstOrNull { it.id == scene.speakerCharacterId || it.name == scene.speakerName }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AnimeDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("player_screen")
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = NeonPurple,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "CINEMA PLAYER & MP4 EXPORT",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Cinematic 2D camera animation • Synced Hindi voice • Real MP4 export",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Live Anime Canvas Player
        if (currentScene != null) {
            AnimeSceneCanvas(
                scene = currentScene,
                character = currentCharacter,
                progress = sceneProgress,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Scene Progress Indicator
            LinearProgressIndicator(
                progress = { sceneProgress },
                color = NeonCyan,
                trackColor = AnimeCardElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Transport Control Bar
            Card(
                colors = CardDefaults.cardColors(containerColor = AnimeCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AnimeBorderGlow, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Scene ${currentSceneIndex + 1} of ${scenes.size}",
                            color = NeonPurpleGlow,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "BGM: ${currentScene.bgmTrack}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Scene
                        IconButton(
                            onClick = {
                                if (currentSceneIndex > 0) viewModel.seekToScene(currentSceneIndex - 1)
                            },
                            enabled = currentSceneIndex > 0,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipPrevious,
                                contentDescription = "Previous Scene",
                                tint = if (currentSceneIndex > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Play/Pause Main Button
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(NeonPurple)
                                .clickable { viewModel.togglePlayPause() }
                                .testTag("play_pause_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Next Scene
                        IconButton(
                            onClick = {
                                if (currentSceneIndex < scenes.size - 1) viewModel.seekToScene(currentSceneIndex + 1)
                            },
                            enabled = currentSceneIndex < scenes.size - 1,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                contentDescription = "Next Scene",
                                tint = if (currentSceneIndex < scenes.size - 1) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Replay from Scene 1
                        IconButton(
                            onClick = {
                                viewModel.seekToScene(0)
                                viewModel.startPlayback()
                            },
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                Icons.Default.Replay,
                                contentDescription = "Replay All",
                                tint = NeonCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Quick Jump Scene Strip
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(scenes) { idx, s ->
                            val isCurrent = idx == currentSceneIndex
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrent) NeonCyan else AnimeCardElevated)
                                    .border(1.dp, if (isCurrent) Color.White else AnimeBorderGlow, RoundedCornerShape(6.dp))
                                    .clickable { viewModel.seekToScene(idx) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    color = if (isCurrent) Color.Black else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = AnimeCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No scenes available. Go to Studio to generate an episode!",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // REAL MP4 VIDEO EXPORT SECTION
        Card(
            colors = CardDefaults.cardColors(containerColor = AnimeCardSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AnimeBorderGlow, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXPORT REAL MP4 VIDEO",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Encodes full H.264 MP4 video using native hardware codecs. Includes camera zoom/pan sequences, anime speed effects, consistent character models, and synchronized Hindi subtitles in 16:9 landscape format.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Resolution Selector
                Text(
                    text = "CHOOSE RESOLUTION:",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Mp4VideoExporter.Resolution.values().forEach { res ->
                        val isSel = res == selectedResolution
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) NeonCyan else AnimeCardElevated)
                                .border(1.dp, if (isSel) Color.White else AnimeBorderGlow, RoundedCornerShape(8.dp))
                            .clickable { selectedResolution = res }
                            .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = res.label,
                                color = if (isSel) Color.Black else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Export Button
                Button(
                    onClick = {
                        viewModel.exportVideo(selectedResolution)
                    },
                    enabled = scenes.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("export_mp4_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Render & Export MP4 (${selectedResolution.label.take(4)})",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Export Progress & Finished Dialog
    when (val state = exportState) {
        is AnimeStudioViewModel.ExportState.Exporting -> {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Text(
                        text = "Rendering Anime Video...",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = NeonPurple,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { state.progress },
                            color = NeonCyan,
                            trackColor = AnimeCardElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "${(state.progress * 100).toInt()}% Completed",
                            color = NeonPurpleGlow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.message,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                },
                confirmButton = {},
                containerColor = AnimeCardSurface
            )
        }
        is AnimeStudioViewModel.ExportState.Finished -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissExportDialog() },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎉 Video Ready!", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "Your Japanese 2D anime episode has been successfully rendered into MP4 format with synchronized Hindi subtitles and camera animations!",
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(AnimeCardElevated, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "File: ${state.videoFile.name}\nSize: ${state.videoFile.length() / 1024} KB\nPath: ${state.videoFile.absolutePath}",
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                shareVideo(context, state.videoFile)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share / Save MP4")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissExportDialog() }) {
                        Text("Done", color = TextSecondary)
                    }
                },
                containerColor = AnimeCardSurface
            )
        }
        is AnimeStudioViewModel.ExportState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissExportDialog() },
                title = { Text(text = "Export Error", color = Color(0xFFEF4444)) },
                text = { Text(text = state.message, color = TextPrimary) },
                confirmButton = {
                    Button(onClick = { viewModel.dismissExportDialog() }) {
                        Text("OK")
                    }
                },
                containerColor = AnimeCardSurface
            )
        }
        else -> {}
    }
}

private fun shareVideo(context: Context, videoFile: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            videoFile
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "My Anime Video - Vikas AI Anime Studio")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Download / Share Anime Video"))
    } catch (_: Exception) {
        // Fallback generic send
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_TEXT, "Video rendered at: ${videoFile.absolutePath}")
            }
            context.startActivity(Intent.createChooser(intent, "Download Anime Video"))
        } catch (_: Exception) {}
    }
}
