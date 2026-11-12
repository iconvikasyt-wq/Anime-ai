package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "scenes",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class SceneEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val sceneIndex: Int,
    val title: String,
    val setting: String = "Tokyo Shibuya Crossing",
    val durationSeconds: Float = 5.0f,
    val cameraMotion: String = "Ken Burns Zoom In", // Ken Burns Zoom In, Zoom Out, Pan Left, Pan Right, Dynamic Close-up, Wide Angle, Impact Shake
    val transition: String = "Flash Cut", // Flash Cut, Cross Dissolve, Glitch, Fade to Black, Cut
    val dialogueHindi: String = "हम यहाँ से ज़िंदा बचकर निकलेंगे!",
    val dialogueTranslation: String = "We will make it out alive from here!",
    val speakerCharacterId: Long? = null,
    val speakerName: String = "Kaito",
    val emotion: String = "Determined", // Determined, Fierce, Shocked, Smug, Smiling, Sorrowful
    val sfxType: String = "Katana Slash", // Katana Slash, Heavy Explosion, Thunder Strike, Heartbeat, Cyber Glitch, Siren, None
    val bgmTrack: String = "Epic Battle", // Epic Battle, Cyberpunk Synth, Horror Dark Ambient, Melancholic Piano, Dramatic Strings
    val customImagePath: String? = null,
    val backgroundTheme: String = "Tokyo Night" // Tokyo Night, Neon Alley, Ancient Shrine, Ruined School, Cyber Skyline, Sunset Rooftop
)
