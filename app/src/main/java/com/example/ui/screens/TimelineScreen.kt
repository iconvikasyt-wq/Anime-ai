package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.SceneEntity
import com.example.ui.theme.AnimeBorderGlow
import com.example.ui.theme.AnimeCardElevated
import com.example.ui.theme.AnimeCardSurface
import com.example.ui.theme.AnimeDarkBg
import com.example.ui.theme.AnimeRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.SubtitleCyan
import com.example.ui.theme.SubtitleGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AnimeStudioViewModel

@Composable
fun TimelineScreen(
    viewModel: AnimeStudioViewModel,
    modifier: Modifier = Modifier
) {
    val scenes by viewModel.activeScenes.collectAsStateWithLifecycle()
    val characters by viewModel.activeCharacters.collectAsStateWithLifecycle()
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()

    var editingScene by remember { mutableStateOf<SceneEntity?>(null) }

    val totalDurationSeconds = remember(scenes) {
        scenes.sumOf { it.durationSeconds.toDouble() }.toFloat()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AnimeDarkBg)
            .padding(16.dp)
            .testTag("timeline_screen")
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timeline,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "VIDEO TIMELINE EDITOR",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Edit dialogues • Reorder scenes • Hindi character voices • Camera choreographies",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Timeline Summary Strip
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AnimeCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AnimeBorderGlow, RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "TOTAL SCENES", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(text = "${scenes.size} Scenes", color = NeonCyan, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Column {
                        Text(text = "RUN TIME", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        val mins = (totalDurationSeconds / 60).toInt()
                        val secs = (totalDurationSeconds % 60).toInt()
                        Text(text = "${mins}m ${secs}s", color = NeonPurpleGlow, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Button(
                        onClick = {
                            viewModel.addSceneAfter(scenes.size - 1)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_scene_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Scene", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // List of Scenes
        itemsIndexed(scenes, key = { _, s -> s.id }) { index, scene ->
            SceneTimelineCard(
                index = index,
                totalScenes = scenes.size,
                scene = scene,
                onPlayPreview = {
                    val char = characters.firstOrNull { it.id == scene.speakerCharacterId || it.name == scene.speakerName }
                    val voice = char?.voiceType ?: "Hero Deep Male"
                    viewModel.voiceEngine.playSfx(scene.sfxType)
                    viewModel.voiceEngine.speakDialogue(scene.dialogueHindi, voice)
                },
                onMoveUp = {
                    if (index > 0) viewModel.moveScene(index, index - 1)
                },
                onMoveDown = {
                    if (index < scenes.size - 1) viewModel.moveScene(index, index + 1)
                },
                onEdit = { editingScene = scene },
                onDelete = { viewModel.deleteScene(scene) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }

    // Edit Scene Dialog
    if (editingScene != null) {
        val scene = editingScene!!
        var title by remember(scene) { mutableStateOf(scene.title) }
        var hindiText by remember(scene) { mutableStateOf(scene.dialogueHindi) }
        var engText by remember(scene) { mutableStateOf(scene.dialogueTranslation) }
        var speakerName by remember(scene) { mutableStateOf(scene.speakerName) }
        var cameraMotion by remember(scene) { mutableStateOf(scene.cameraMotion) }
        var sfxType by remember(scene) { mutableStateOf(scene.sfxType) }
        var bgmTrack by remember(scene) { mutableStateOf(scene.bgmTrack) }
        var durationSec by remember(scene) { mutableFloatStateOf(scene.durationSeconds) }
        var emotion by remember(scene) { mutableStateOf(scene.emotion) }
        var bgTheme by remember(scene) { mutableStateOf(scene.backgroundTheme) }

        val cameraOptions = listOf("Ken Burns Zoom In", "Ken Burns Zoom Out", "Pan Left", "Pan Right", "Dynamic Close-up", "Wide Cinematic", "Impact Shake")
        val sfxOptions = listOf("Katana Slash", "Heavy Explosion", "Thunder Strike", "Heartbeat", "Cyber Glitch", "Siren", "None")
        val bgmOptions = listOf("Epic Battle", "Cyberpunk Synth", "Horror Dark Ambient", "Melancholic Piano", "Dramatic Strings")
        val themeOptions = listOf("Tokyo Shibuya Crossing", "Cyberpunk Neon Alley", "Ancient Torii Shrine", "High School Rooftop Dusk", "Blood Moon Shibuya Alley")

        AlertDialog(
            onDismissRequest = { editingScene = null },
            title = {
                Text(
                    text = "Edit Scene ${scene.sceneIndex + 1}",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Scene Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Hindi Dialogue
                    OutlinedTextField(
                        value = hindiText,
                        onValueChange = { hindiText = it },
                        label = { Text("Hindi Dialogue (हिंदी संवाद)") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // English Translation
                    OutlinedTextField(
                        value = engText,
                        onValueChange = { engText = it },
                        label = { Text("English Translation / Subtitle") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Speaker Selection
                    Text(text = "Speaker Character:", color = TextSecondary, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val speakerChoices = characters.map { it.name }.plus("Narrator")
                        speakerChoices.take(4).forEach { name ->
                            val isSel = speakerName.contains(name)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonPurple else AnimeCardElevated)
                                    .clickable { speakerName = name }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(text = name, color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Duration Slider
                    Text(text = "Duration: ${String.format("%.1f", durationSec)}s", color = TextPrimary, fontSize = 12.sp)
                    Slider(
                        value = durationSec,
                        onValueChange = { durationSec = it },
                        valueRange = 2.0f..15.0f,
                        steps = 13,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonCyan,
                            inactiveTrackColor = AnimeBorderGlow
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Camera Motion Choice
                    Text(text = "Camera Angle: $cameraMotion", color = NeonPurpleGlow, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        cameraOptions.take(3).forEach { cam ->
                            val isSel = cam == cameraMotion
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonCyan else AnimeCardElevated)
                                    .clickable { cameraMotion = cam }
                                    .padding(horizontal = 6.dp, vertical = 5.dp)
                            ) {
                                Text(text = cam.take(12), color = if (isSel) Color.Black else TextPrimary, fontSize = 9.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // SFX Choice
                    Text(text = "Sound Effect (SFX): $sfxType", color = NeonPink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        sfxOptions.take(4).forEach { sfx ->
                            val isSel = sfx == sfxType
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSel) NeonPink else AnimeCardElevated)
                                    .clickable { sfxType = sfx }
                                    .padding(horizontal = 6.dp, vertical = 5.dp)
                            ) {
                                Text(text = sfx, color = Color.White, fontSize = 9.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val matchedChar = characters.firstOrNull { it.name == speakerName }
                        viewModel.updateScene(
                            scene.copy(
                                title = title,
                                dialogueHindi = hindiText,
                                dialogueTranslation = engText,
                                speakerName = speakerName,
                                speakerCharacterId = matchedChar?.id,
                                cameraMotion = cameraMotion,
                                sfxType = sfxType,
                                bgmTrack = bgmTrack,
                                durationSeconds = durationSec,
                                emotion = emotion,
                                backgroundTheme = bgTheme
                            )
                        )
                        editingScene = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingScene = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AnimeCardSurface
        )
    }
}

@Composable
private fun SceneTimelineCard(
    index: Int,
    totalScenes: Int,
    scene: SceneEntity,
    onPlayPreview: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AnimeCardSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AnimeBorderGlow, RoundedCornerShape(12.dp))
            .testTag("scene_card_$index")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Scene #, Duration, Camera, Reorder
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .background(NeonPurple.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                        .border(1.dp, NeonPurple, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SCENE #${index + 1}",
                        color = NeonPurpleGlow,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .background(AnimeCardElevated, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${scene.durationSeconds}s",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "🎥 ${scene.cameraMotion}",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.weight(1f))

                // Reorder arrows
                IconButton(
                    onClick = onMoveUp,
                    enabled = index > 0,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = if (index > 0) TextPrimary else TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
                IconButton(
                    onClick = onMoveDown,
                    enabled = index < totalScenes - 1,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = if (index < totalScenes - 1) TextPrimary else TextSecondary.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Speaker Badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(NeonPink, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = scene.speakerName,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${scene.setting}",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                if (scene.sfxType != "None") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "⚡ ${scene.sfxType}",
                        color = NeonPink,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hindi Dialogue Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F0E20))
                    .border(1.dp, AnimeBorderGlow, RoundedCornerShape(6.dp))
                    .padding(8.dp)
            ) {
                Column {
                    Text(
                        text = scene.dialogueHindi,
                        color = SubtitleGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = scene.dialogueTranslation,
                        color = SubtitleCyan,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice preview button
                Button(
                    onClick = onPlayPreview,
                    colors = ButtonDefaults.buttonColors(containerColor = AnimeCardElevated),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hear Hindi Voice", fontSize = 10.sp, color = NeonCyan)
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Scene", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Scene", tint = AnimeRed, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
