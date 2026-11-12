package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AnimeStudioTab
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
import com.example.viewmodel.AnimeStudioViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(
    viewModel: AnimeStudioViewModel,
    onNavigateTab: (AnimeStudioTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeProject by viewModel.activeProject.collectAsStateWithLifecycle()
    val characters by viewModel.activeCharacters.collectAsStateWithLifecycle()
    val scenes by viewModel.activeScenes.collectAsStateWithLifecycle()
    val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()

    var promptText by remember {
        mutableStateOf("Create a 15-minute Japanese zombie horror anime in Tokyo with 5 main characters.")
    }
    var selectedDurationMinutes by remember { mutableIntStateOf(15) }

    val samplePrompts = listOf(
        "Tokyo zombie horror in Shibuya with 5 high schoolers",
        "Cyberpunk shinobi rogue fighting an AI mega-corporation in Neo-Kyoto",
        "Astral academy elemental sorcerers battling in imperial tournament",
        "Orbital Mecha titan pilots defending space station above Earth"
    )

    val durationOptions = listOf(5, 10, 15, 20, 30)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AnimeDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_screen")
    ) {
        // Hero Visual Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 8.5f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.5.dp, AnimeBorderGlow, RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.anime_studio_hero),
                contentDescription = "Vikas Anime Studio Key Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Vignette gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color(0xCC0B0B14), Color(0xF20B0B14))
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(NeonPink, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "2D ANIME STUDIO",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "「ビカスAIアニメスタジオ」",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "VIKAS AI ANIME STUDIO",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Single-Prompt Story • Multi-Character Consistency • Hindi Voices • MP4 Video",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Active Project Info Card
        if (activeProject != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = AnimeCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AnimeBorderGlow, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(NeonCyan)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ACTIVE PROJECT",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${scenes.size} Scenes Generated",
                            color = NeonPurpleGlow,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activeProject!!.title,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = activeProject!!.synopsis,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Jump Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigateTab(AnimeStudioTab.CHARACTERS) },
                            colors = ButtonDefaults.buttonColors(containerColor = AnimeCardElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("dashboard_btn_characters")
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Characters (${characters.size})", fontSize = 11.sp, color = TextPrimary)
                        }
                        Button(
                            onClick = { onNavigateTab(AnimeStudioTab.TIMELINE) },
                            colors = ButtonDefaults.buttonColors(containerColor = AnimeCardElevated),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("dashboard_btn_timeline")
                        ) {
                            Icon(Icons.Default.Timeline, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Timeline (${scenes.size})", fontSize = 11.sp, color = TextPrimary)
                        }
                        Button(
                            onClick = { onNavigateTab(AnimeStudioTab.CINEMA) },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("dashboard_btn_cinema")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // ONE PROMPT ANIME GENERATOR Section
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
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Generator Icon",
                        tint = NeonPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ONE-PROMPT ANIME GENERATOR",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter a single story prompt. The engine will write the full narrative, choreograph 30-60 scenes, generate camera instructions, write Hindi character dialogues, and link character models.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Prompt Input Box
                OutlinedTextField(
                    value = promptText,
                    onValueChange = { promptText = it },
                    label = { Text("Story Prompt (कहानी का विचार)") },
                    minLines = 3,
                    maxLines = 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = AnimeBorderGlow,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonPurple
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("story_prompt_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Quick Prompt Presets
                Text(
                    text = "QUICK STORY PRESETS:",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    samplePrompts.forEach { preset ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AnimeCardElevated)
                                .border(1.dp, AnimeBorderGlow, RoundedCornerShape(6.dp))
                                .clickable { promptText = preset }
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = preset,
                                color = NeonCyan,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Video Duration Selector
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "EPISODE DURATION & SCENE DENSITY:",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    durationOptions.forEach { duration ->
                        val isSelected = duration == selectedDurationMinutes
                        val sceneCount = when (duration) {
                            5 -> "15 Scenes"
                            10 -> "25 Scenes"
                            15 -> "35 Scenes"
                            20 -> "45 Scenes"
                            else -> "55 Scenes"
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonPurple else AnimeCardElevated)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) NeonPurpleGlow else AnimeBorderGlow,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedDurationMinutes = duration }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$duration m",
                                    color = if (isSelected) Color.White else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = sceneCount,
                                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextSecondary,
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Generation Button
                Button(
                    onClick = {
                        if (promptText.isNotBlank()) {
                            viewModel.generateNewAnimeProject(promptText, selectedDurationMinutes)
                        }
                    },
                    enabled = !isGenerating && promptText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonPurple,
                        disabledContainerColor = AnimeCardElevated
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("generate_anime_button")
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Writing Anime Story & Scripting Scenes...",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Generate Japanese Anime Episode",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Features Checklist Card
        Card(
            colors = CardDefaults.cardColors(containerColor = AnimeCardSurface),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AnimeBorderGlow, RoundedCornerShape(12.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "STUDIO SPECIFICATIONS",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FeatureRow(title = "Multi-Character Sheet Extraction", desc = "Upload 1 image with multiple characters, crop & link profiles")
                FeatureRow(title = "Character Consistency Lock", desc = "Identical face, hair, clothing & abilities across all scenes")
                FeatureRow(title = "Native Hindi Voice Generator", desc = "Hindi dialogues & narration with distinct character pitch modulation")
                FeatureRow(title = "Cinematic Camera & Speed Lines", desc = "Ken Burns zoom, dynamic panning, impact shake & transitions")
                FeatureRow(title = "Real MP4 Video Export", desc = "720p/1080p landscape video render with burned-in subtitles")
            }
        }
    }
}

@Composable
private fun FeatureRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(NeonPurple)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            Text(text = desc, color = TextSecondary, fontSize = 10.sp)
        }
    }
}
