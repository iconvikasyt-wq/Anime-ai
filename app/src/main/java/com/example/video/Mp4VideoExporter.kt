package com.example.video

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.os.Environment
import android.view.Surface
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.SceneEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.sin

object Mp4VideoExporter {

    data class ExportConfig(
        val width: Int = 1280,
        val height: Int = 720,
        val frameRate: Int = 30,
        val bitRate: Int = 4_000_000
    )

    enum class Resolution(val width: Int, val height: Int, val label: String) {
        HD_720P(1280, 720, "720p (1280x720)"),
        FHD_1080P(1920, 1080, "1080p (1920x1080)")
    }

    suspend fun exportAnimeVideo(
        context: Context,
        scenes: List<SceneEntity>,
        characters: List<CharacterEntity>,
        resolution: Resolution = Resolution.HD_720P,
        onProgress: (progress: Float, currentScene: Int, totalScenes: Int, message: String) -> Unit
    ): File? = withContext(Dispatchers.IO) {
        if (scenes.isEmpty()) return@withContext null

        val width = resolution.width
        val height = resolution.height
        val frameRate = 30
        val bitRate = if (resolution == Resolution.FHD_1080P) 8_000_000 else 4_500_000

        // Create output file
        val outputDir = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES) ?: context.cacheDir
        if (!outputDir.exists()) outputDir.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outputFile = File(outputDir, "VikasAnime_${resolution.label.take(4)}_$timestamp.mp4")

        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var inputSurface: Surface? = null

        try {
            val format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
                setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            inputSurface = codec.createInputSurface()
            codec.start()

            muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            var videoTrackIndex = -1
            var muxerStarted = false

            // Pre-load character portraits
            val characterBitmaps = mutableMapOf<Long, Bitmap>()
            for (char in characters) {
                if (char.croppedImagePath != null) {
                    val file = File(char.croppedImagePath)
                    if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)?.let {
                            characterBitmaps[char.id] = it
                        }
                    }
                }
            }

            // Paints setup
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = (height * 0.045f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                setShadowLayer(8f, 0f, 0f, Color.BLACK)
            }

            val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#38BDF8")
                textSize = (height * 0.032f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                setShadowLayer(6f, 0f, 0f, Color.BLACK)
            }

            val speakerBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#EC4899")
                textSize = (height * 0.030f)
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val speedLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(90, 255, 255, 255)
                strokeWidth = 3f
            }

            val bufferInfo = MediaCodec.BufferInfo()
            var presentationTimeUs = 0L
            val frameDurationUs = (1_000_000L / frameRate)

            val totalScenes = scenes.size
            var globalFrameCount = 0
            val totalEstimatedFrames = scenes.sumOf { (it.durationSeconds * frameRate).toInt() }

            // Encode scene by scene
            for ((sceneIndex, scene) in scenes.withIndex()) {
                val sceneFrames = (scene.durationSeconds * frameRate).toInt().coerceAtLeast(15)
                val characterBmp = scene.speakerCharacterId?.let { characterBitmaps[it] }
                    ?: characterBitmaps.values.firstOrNull()

                for (frame in 0 until sceneFrames) {
                    val sceneProgress = frame.toFloat() / sceneFrames.toFloat()

                    // Render onto surface canvas
                    val canvas = inputSurface.lockCanvas(null)
                    if (canvas != null) {
                        try {
                            renderSceneToCanvas(
                                canvas = canvas,
                                width = width,
                                height = height,
                                scene = scene,
                                sceneProgress = sceneProgress,
                                characterBmp = characterBmp,
                                textPaint = textPaint,
                                subtitlePaint = subtitlePaint,
                                speakerBadgePaint = speakerBadgePaint,
                                speedLinePaint = speedLinePaint
                            )
                        } finally {
                            inputSurface.unlockCanvasAndPost(canvas)
                        }
                    }

                    // Drain encoder
                    drainEncoder(
                        codec = codec,
                        muxer = muxer,
                        bufferInfo = bufferInfo,
                        isEos = false,
                        getTrackIndex = { videoTrackIndex },
                        setTrackIndex = { videoTrackIndex = it },
                        isMuxerStarted = { muxerStarted },
                        setMuxerStarted = { muxerStarted = it }
                    )

                    presentationTimeUs += frameDurationUs
                    globalFrameCount++

                    if (globalFrameCount % 10 == 0 || frame == sceneFrames - 1) {
                        val progress = (globalFrameCount.toFloat() / totalEstimatedFrames.toFloat()).coerceIn(0f, 0.98f)
                        onProgress(progress, sceneIndex + 1, totalScenes, "Rendering Scene ${sceneIndex + 1}/$totalScenes: ${scene.title}")
                    }
                }
            }

            // Signal End of Stream
            codec.signalEndOfInputStream()
            drainEncoder(
                codec = codec,
                muxer = muxer,
                bufferInfo = bufferInfo,
                isEos = true,
                getTrackIndex = { videoTrackIndex },
                setTrackIndex = { videoTrackIndex = it },
                isMuxerStarted = { muxerStarted },
                setMuxerStarted = { muxerStarted = it }
            )

            onProgress(1.0f, totalScenes, totalScenes, "Rendering complete! MP4 saved.")
            return@withContext outputFile

        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        } finally {
            try {
                codec?.stop()
                codec?.release()
                inputSurface?.release()
                muxer?.stop()
                muxer?.release()
            } catch (_: Exception) {}
        }
    }

    private fun renderSceneToCanvas(
        canvas: Canvas,
        width: Int,
        height: Int,
        scene: SceneEntity,
        sceneProgress: Float,
        characterBmp: Bitmap?,
        textPaint: Paint,
        subtitlePaint: Paint,
        speakerBadgePaint: Paint,
        speedLinePaint: Paint
    ) {
        // 1. Draw stylized Japanese anime background with gradient
        val bgPaint = Paint()
        val (topColor, bottomColor) = when (scene.backgroundTheme) {
            "Cyberpunk Neon Alley" -> Pair(Color.parseColor("#180033"), Color.parseColor("#061727"))
            "Ancient Torii Shrine" -> Pair(Color.parseColor("#3B0764"), Color.parseColor("#7F1D1D"))
            "Blood Moon Shibuya Alley" -> Pair(Color.parseColor("#450A0A"), Color.parseColor("#1E1B4B"))
            "High School Rooftop Dusk" -> Pair(Color.parseColor("#701A75"), Color.parseColor("#B45309"))
            else -> Pair(Color.parseColor("#0F172A"), Color.parseColor("#1E1B4B"))
        }

        bgPaint.shader = LinearGradient(0f, 0f, 0f, height.toFloat(), topColor, bottomColor, Shader.TileMode.CLAMP)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // 2. Camera Motion Transforms
        canvas.save()
        when (scene.cameraMotion) {
            "Ken Burns Zoom In" -> {
                val scale = 1.0f + sceneProgress * 0.12f
                canvas.scale(scale, scale, width / 2f, height / 2f)
            }
            "Ken Burns Zoom Out" -> {
                val scale = 1.12f - sceneProgress * 0.12f
                canvas.scale(scale, scale, width / 2f, height / 2f)
            }
            "Pan Left" -> {
                val dx = sceneProgress * (width * 0.08f)
                canvas.translate(-dx, 0f)
            }
            "Pan Right" -> {
                val dx = sceneProgress * (width * 0.08f)
                canvas.translate(dx, 0f)
            }
            "Impact Shake" -> {
                val shake = (sin(sceneProgress * 40.0) * (height * 0.015f) * (1f - sceneProgress)).toFloat()
                canvas.translate(0f, shake)
            }
        }

        // Decorative anime neon grid & skyline silhouette
        val gridPaint = Paint().apply {
            color = Color.argb(45, 168, 85, 247)
            strokeWidth = 2f
        }
        val horizonY = height * 0.65f
        for (x in 0..width step (width / 16)) {
            canvas.drawLine(x.toFloat(), horizonY, (x + (x - width / 2) * 1.5f), height.toFloat(), gridPaint)
        }
        canvas.drawLine(0f, horizonY, width.toFloat(), horizonY, gridPaint)

        // Draw anime moon / sun
        val celestialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (scene.backgroundTheme.contains("Blood")) Color.parseColor("#EF4444") else Color.parseColor("#06B6D4")
            setShadowLayer(40f, 0f, 0f, color)
        }
        canvas.drawCircle(width * 0.8f, height * 0.28f, height * 0.14f, celestialPaint)

        // Action Speed Lines if action scene
        if (scene.emotion == "Fierce" || scene.sfxType == "Katana Slash") {
            for (i in 0..12) {
                val lineY = (i * (height / 12f)) + (sceneProgress * 30f) % (height / 12f)
                canvas.drawLine(0f, lineY, width.toFloat(), lineY + (i % 3) * 10f, speedLinePaint)
            }
        }

        // Draw Character Portrait if present
        if (characterBmp != null) {
            val charW = (height * 0.72f).toInt()
            val charH = (height * 0.78f).toInt()
            val charX = (width * 0.10f).toInt()
            val charY = (height - charH).coerceAtLeast(0)

            val destRect = Rect(charX, charY, charX + charW, charY + charH)
            val srcRect = Rect(0, 0, characterBmp.width, characterBmp.height)

            // Subtle anime breathing bounce
            val bounceY = (sin(sceneProgress * Math.PI * 2) * 8f).toInt()
            destRect.offset(0, bounceY)

            canvas.drawBitmap(characterBmp, srcRect, destRect, null)
        }

        canvas.restore()

        // 3. Cinematic Letterbox Bars
        val barHeight = height * 0.10f
        val letterboxPaint = Paint().apply { color = Color.BLACK }
        canvas.drawRect(0f, 0f, width.toFloat(), barHeight, letterboxPaint)
        canvas.drawRect(0f, height - barHeight, width.toFloat(), height.toFloat(), letterboxPaint)

        // 4. Subtitle Card Banner at Bottom
        val bannerY = height - barHeight - (height * 0.16f)
        val bannerHeight = height * 0.15f
        val bannerPaint = Paint().apply {
            color = Color.argb(200, 10, 10, 20)
        }
        val bannerRect = RectF(width * 0.05f, bannerY, width * 0.95f, bannerY + bannerHeight)
        canvas.drawRoundRect(bannerRect, 16f, 16f, bannerPaint)

        // Glowing border on banner
        val borderPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.parseColor("#A855F7")
        }
        canvas.drawRoundRect(bannerRect, 16f, 16f, borderPaint)

        // Speaker Name Badge
        canvas.drawText("【 ${scene.speakerName} 】", width * 0.08f, bannerY + (height * 0.045f), speakerBadgePaint)

        // Hindi Dialogue Text
        canvas.drawText(scene.dialogueHindi, width * 0.08f, bannerY + (height * 0.095f), textPaint)

        // English Subtitle Translation
        canvas.drawText(scene.dialogueTranslation, width * 0.08f, bannerY + (height * 0.135f), subtitlePaint)

        // Top Studio Watermark & Scene Index
        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = (height * 0.030f)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("VIKAS AI ANIME STUDIO 「アニメ」", width * 0.05f, barHeight * 0.65f, watermarkPaint)
        canvas.drawText("${scene.title} | ${scene.durationSeconds}s", width * 0.70f, barHeight * 0.65f, watermarkPaint)
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        isEos: Boolean,
        getTrackIndex: () -> Int,
        setTrackIndex: (Int) -> Unit,
        isMuxerStarted: () -> Boolean,
        setMuxerStarted: (Boolean) -> Unit
    ) {
        val timeoutUs = 10_000L
        while (true) {
            val encoderStatus = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!isEos) break
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (isMuxerStarted()) {
                    throw RuntimeException("Format changed twice")
                }
                val newFormat = codec.outputFormat
                val track = muxer.addTrack(newFormat)
                setTrackIndex(track)
                muxer.start()
                setMuxerStarted(true)
            } else if (encoderStatus >= 0) {
                val encodedData = codec.getOutputBuffer(encoderStatus) ?: continue
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    bufferInfo.size = 0
                }

                if (bufferInfo.size != 0) {
                    if (!isMuxerStarted()) {
                        throw RuntimeException("Muxer hasn't started")
                    }
                    encodedData.position(bufferInfo.offset)
                    encodedData.limit(bufferInfo.offset + bufferInfo.size)
                    muxer.writeSampleData(getTrackIndex(), encodedData, bufferInfo)
                }

                codec.releaseOutputBuffer(encoderStatus, false)

                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    break
                }
            }
        }
    }
}
