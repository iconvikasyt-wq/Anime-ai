package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale
import kotlin.concurrent.thread
import kotlin.math.sin

class HindiVoiceEngine(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    var isInitialized = false
        private set
    var isHindiSupported = false
        private set

    private var onSpeechDoneCallback: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("hi", "IN"))
            isHindiSupported = when (result) {
                TextToSpeech.LANG_MISSING_DATA, TextToSpeech.LANG_NOT_SUPPORTED -> {
                    // Try generic "hi"
                    val genericResult = tts?.setLanguage(Locale("hi"))
                    genericResult != TextToSpeech.LANG_MISSING_DATA && genericResult != TextToSpeech.LANG_NOT_SUPPORTED
                }
                else -> true
            }
            isInitialized = true

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    onSpeechDoneCallback?.invoke()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    onSpeechDoneCallback?.invoke()
                }
            })
        }
    }

    /**
     * Speaks dialogue with character-specific pitch and speech rate
     */
    fun speakDialogue(
        text: String,
        voiceType: String,
        onDone: (() -> Unit)? = null
    ) {
        if (!isInitialized) {
            onDone?.invoke()
            return
        }

        onSpeechDoneCallback = onDone

        // Configure pitch and speed based on character voice type
        val (pitch, speed) = when (voiceType) {
            "Hero Deep Male" -> Pair(0.85f, 0.98f)
            "Energetic Youth" -> Pair(1.20f, 1.10f)
            "Soft Female" -> Pair(1.35f, 1.00f)
            "Dramatic Villain" -> Pair(0.68f, 0.88f)
            "Elder Narrator" -> Pair(0.90f, 0.90f)
            else -> Pair(1.0f, 1.0f)
        }

        tts?.setPitch(pitch)
        tts?.setSpeechRate(speed)

        // Try to pick matching gender voice if system provides multiple Hindi voices
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                val hindiVoices = voices.filter { it.locale.language == "hi" }
                val targetVoice = when (voiceType) {
                    "Soft Female" -> hindiVoices.firstOrNull { it.name.lowercase().contains("female") }
                    "Hero Deep Male", "Dramatic Villain" -> hindiVoices.firstOrNull { it.name.lowercase().contains("male") }
                    else -> null
                }
                if (targetVoice != null) {
                    tts?.voice = targetVoice
                }
            }
        } catch (_: Exception) {
            // Fallback to default voice modulation
        }

        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "dialogue_${System.currentTimeMillis()}")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "dialogue_${System.currentTimeMillis()}")
    }

    /**
     * Stops currently playing speech
     */
    fun stop() {
        try {
            tts?.stop()
        } catch (_: Exception) {}
    }

    /**
     * Plays synthetic sound effects for anime action (Katana Slash, Explosion, etc.)
     */
    fun playSfx(sfxType: String) {
        if (sfxType == "None") return

        thread {
            try {
                val sampleRate = 44100
                val durationMs = when (sfxType) {
                    "Katana Slash" -> 350
                    "Heavy Explosion" -> 800
                    "Thunder Strike" -> 600
                    "Heartbeat" -> 400
                    "Cyber Glitch" -> 300
                    "Siren" -> 700
                    else -> 300
                }
                val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
                val buffer = ShortArray(numSamples)

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples.toDouble()
                    val decay = 1.0 - progress
                    val sample = when (sfxType) {
                        "Katana Slash" -> {
                            // Chirp from 1800Hz down to 300Hz with white noise burst
                            val freq = 1800.0 * (1.0 - progress * 0.8)
                            val wave = sin(2.0 * Math.PI * freq * i / sampleRate)
                            val noise = (Math.random() * 2.0 - 1.0) * 0.4
                            (wave * 0.6 + noise) * decay
                        }
                        "Heavy Explosion" -> {
                            // Low frequency rumble + noise
                            val freq = 90.0 * (1.0 - progress * 0.6)
                            val wave = sin(2.0 * Math.PI * freq * i / sampleRate)
                            val noise = (Math.random() * 2.0 - 1.0) * 0.7
                            (wave * 0.4 + noise) * (decay * decay)
                        }
                        "Thunder Strike" -> {
                            // High initial transient + resonance
                            val noise = (Math.random() * 2.0 - 1.0)
                            val rumble = sin(2.0 * Math.PI * 65.0 * i / sampleRate) * 0.5
                            (noise * 0.7 + rumble) * (decay * 1.2).coerceAtMost(1.0)
                        }
                        "Heartbeat" -> {
                            // Double thump
                            val pulse = if (progress < 0.35 || (progress > 0.45 && progress < 0.8)) {
                                sin(2.0 * Math.PI * 55.0 * i / sampleRate)
                            } else 0.0
                            pulse * 0.9
                        }
                        "Cyber Glitch" -> {
                            val f = if ((i / 400) % 2 == 0) 2200.0 else 1400.0
                            sin(2.0 * Math.PI * f * i / sampleRate) * decay
                        }
                        "Siren" -> {
                            val f = 600.0 + 300.0 * sin(2.0 * Math.PI * 3.0 * progress)
                            sin(2.0 * Math.PI * f * i / sampleRate) * 0.7
                        }
                        else -> 0.0
                    }
                    buffer[i] = (sample * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()
                Thread.sleep(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }

    /**
     * Releases TTS resources
     */
    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }
}
