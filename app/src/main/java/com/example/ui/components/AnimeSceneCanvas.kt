package com.example.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.SceneEntity
import com.example.ui.theme.AnimeBorderGlow
import com.example.ui.theme.AnimeCardSurface
import com.example.ui.theme.AnimeDarkBg
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.SubtitleCyan
import com.example.ui.theme.SubtitleGold
import java.io.File
import kotlin.math.sin

@Composable
fun AnimeSceneCanvas(
    scene: SceneEntity,
    character: CharacterEntity?,
    progress: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Infinite animation for breathing and ambient particles
    val infiniteTransition = rememberInfiniteTransition(label = "anime_ambient")
    val ambientPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Load character bitmap if available
    val characterBitmap = remember(character?.croppedImagePath) {
        if (character?.croppedImagePath != null) {
            val file = File(character.croppedImagePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
            } else null
        } else null
    }

    // Determine theme gradient colors
    val (topColor, bottomColor) = remember(scene.backgroundTheme) {
        when (scene.backgroundTheme) {
            "Cyberpunk Neon Alley" -> Pair(Color(0xFF180033), Color(0xFF061727))
            "Ancient Torii Shrine" -> Pair(Color(0xFF3B0764), Color(0xFF7F1D1D))
            "Blood Moon Shibuya Alley" -> Pair(Color(0xFF450A0A), Color(0xFF1E1B4B))
            "High School Rooftop Dusk" -> Pair(Color(0xFF701A75), Color(0xFFB45309))
            "Secret Cybernetic Lab" -> Pair(Color(0xFF042F2E), Color(0xFF0F172A))
            else -> Pair(Color(0xFF0F172A), Color(0xFF1E1B4B))
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, AnimeBorderGlow, RoundedCornerShape(12.dp))
            .background(AnimeDarkBg)
            .testTag("anime_scene_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw Japanese Anime Background Gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(topColor, bottomColor)
                )
            )

            // 2. Camera Motion Transforms
            val effectiveProgress = if (isPlaying) progress else 0.5f

            var scaleX = 1.0f
            var scaleY = 1.0f
            var transX = 0f
            var transY = 0f

            when (scene.cameraMotion) {
                "Ken Burns Zoom In" -> {
                    val s = 1.0f + effectiveProgress * 0.15f
                    scaleX = s
                    scaleY = s
                }
                "Ken Burns Zoom Out" -> {
                    val s = 1.15f - effectiveProgress * 0.15f
                    scaleX = s
                    scaleY = s
                }
                "Pan Left" -> {
                    transX = -effectiveProgress * (canvasW * 0.10f)
                }
                "Pan Right" -> {
                    transX = effectiveProgress * (canvasW * 0.10f)
                }
                "Impact Shake" -> {
                    transY = (sin(effectiveProgress * 30.0) * (canvasH * 0.02f) * (1f - effectiveProgress)).toFloat()
                }
            }

            scale(scaleX, scaleY, Offset(canvasW / 2f, canvasH / 2f)) {
                translate(transX, transY) {
                    // Cyberpunk / Anime Grid
                    val horizon = canvasH * 0.65f
                    val gridColor = Color(0x33A855F7)
                    for (step in 0..16) {
                        val startX = (step * (canvasW / 16f))
                        val endX = startX + (startX - canvasW / 2f) * 1.6f
                        drawLine(
                            color = gridColor,
                            start = Offset(startX, horizon),
                            end = Offset(endX, canvasH),
                            strokeWidth = 2f
                        )
                    }
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, horizon),
                        end = Offset(canvasW, horizon),
                        strokeWidth = 2f
                    )

                    // Celestial Orb (Neon Moon / Sun)
                    val orbColor = if (scene.backgroundTheme.contains("Blood")) Color(0xFFEF4444) else NeonCyan
                    drawCircle(
                        color = orbColor.copy(alpha = 0.85f),
                        radius = canvasH * 0.16f,
                        center = Offset(canvasW * 0.82f, canvasH * 0.30f)
                    )
                    drawCircle(
                        color = orbColor.copy(alpha = 0.25f),
                        radius = canvasH * 0.22f + ambientPulse * 8f,
                        center = Offset(canvasW * 0.82f, canvasH * 0.30f)
                    )

                    // Action Speed Lines during fierce/slash scene
                    if (scene.emotion == "Fierce" || scene.sfxType == "Katana Slash") {
                        for (i in 0..10) {
                            val lineY = (i * (canvasH / 10f) + effectiveProgress * 25f) % canvasH
                            drawLine(
                                color = Color.White.copy(alpha = 0.35f),
                                start = Offset(0f, lineY),
                                end = Offset(canvasW, lineY + (i % 2) * 8f),
                                strokeWidth = 3f
                            )
                        }
                    }

                    // Ambient Anime Embers / Petals
                    for (p in 0..14) {
                        val px = (canvasW * ((p * 0.17f + effectiveProgress * 0.3f) % 1.0f))
                        val py = (canvasH * ((p * 0.23f + effectiveProgress * 0.4f) % 1.0f))
                        val pAlpha = (0.2f + 0.5f * sin((p + effectiveProgress * 5).toDouble())).toFloat().coerceIn(0f, 1f)
                        drawCircle(
                            color = NeonPurple.copy(alpha = pAlpha),
                            radius = (2.5f + (p % 3) * 1.5f),
                            center = Offset(px, py)
                        )
                    }

                    // Character Portrait
                    if (characterBitmap != null) {
                        val charW = (canvasH * 0.70f).toInt()
                        val charH = (canvasH * 0.82f).toInt()
                        val charX = (canvasW * 0.12f).toInt()
                        val bounceY = (sin(effectiveProgress * Math.PI * 2) * 6f).toInt()
                        val charY = (canvasH - charH + bounceY).toInt().coerceAtLeast(0)

                        drawImage(
                            image = characterBitmap,
                            dstOffset = IntOffset(charX, charY),
                            dstSize = IntSize(charW, charH)
                        )
                    }
                }
            }

            // Cinematic Top and Bottom Letterbox Bars
            val letterboxHeight = canvasH * 0.08f
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, 0f),
                size = androidx.compose.ui.geometry.Size(canvasW, letterboxHeight)
            )
            drawRect(
                color = Color.Black,
                topLeft = Offset(0f, canvasH - letterboxHeight),
                size = androidx.compose.ui.geometry.Size(canvasW, letterboxHeight)
            )
        }

        // Top Scene Tag & Camera Overlay
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .border(1.dp, NeonPurple.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "VIKAS ANIME • ${scene.title}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "🎥 ${scene.cameraMotion} | ⚡ ${scene.sfxType}",
                    color = NeonCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Bottom Subtitle & Speaker Card
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .background(Color(0xE60A0A16), RoundedCornerShape(8.dp))
                .border(1.dp, NeonPurple.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
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
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "「${scene.emotion}」",
                        color = NeonPurpleGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Hindi Dialogue
                Text(
                    text = scene.dialogueHindi,
                    color = SubtitleGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 17.sp
                )
                // English Subtitle
                Text(
                    text = scene.dialogueTranslation,
                    color = SubtitleCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
