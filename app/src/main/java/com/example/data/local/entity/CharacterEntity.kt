package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "characters",
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
data class CharacterEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long,
    val name: String,
    val age: Int = 17,
    val gender: String = "Male",
    val personality: String = "Determined Hero",
    val hairColor: String = "Crimson Red",
    val hairstyle: String = "Spiky Anime",
    val eyeColor: String = "Fiery Gold",
    val clothing: String = "High-tech Battle Jacket",
    val height: String = "175 cm",
    val specialAbilities: String = "Flame Katana Burst",
    val storyRole: String = "Protagonist", // Protagonist, Antagonist, Mentor, Comic Relief, Rival, Supporting
    val voiceType: String = "Hero Deep Male", // Hero Deep Male, Energetic Youth, Soft Female, Dramatic Villain, Elder Narrator
    val croppedImagePath: String? = null,
    val sheetImagePath: String? = null
)
