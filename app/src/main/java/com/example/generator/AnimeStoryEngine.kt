package com.example.generator

import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.SceneEntity
import java.util.Locale

object AnimeStoryEngine {

    data class StoryScript(
        val title: String,
        val genre: String,
        val synopsis: String,
        val scenes: List<SceneEntity>
    )

    private val BACKGROUND_THEMES = listOf(
        "Tokyo Shibuya Crossing",
        "Cyberpunk Neon Alley",
        "Ancient Torii Shrine",
        "High School Rooftop Dusk",
        "Abandoned Underground Subway",
        "Neo-Tokyo Sky Fortress",
        "Blood Moon Shibuya Alley",
        "Secret Cybernetic Lab",
        "Rainy Shinjuku Street",
        "Burning Imperial Grounds"
    )

    private val CAMERA_MOTIONS = listOf(
        "Ken Burns Zoom In",
        "Ken Burns Zoom Out",
        "Pan Left",
        "Pan Right",
        "Dynamic Close-up",
        "Wide Cinematic",
        "Impact Shake"
    )

    private val TRANSITIONS = listOf(
        "Flash Cut",
        "Cross Dissolve",
        "Glitch",
        "Fade to Black",
        "Cut"
    )

    private val SFX_LIST = listOf(
        "Katana Slash",
        "Heavy Explosion",
        "Thunder Strike",
        "Heartbeat",
        "Cyber Glitch",
        "Siren",
        "None"
    )

    private val BGM_LIST = listOf(
        "Epic Battle",
        "Cyberpunk Synth",
        "Horror Dark Ambient",
        "Melancholic Piano",
        "Dramatic Strings"
    )

    /**
     * Generates a complete episodic story with scenes tailored to user prompt, duration, and characters
     */
    fun generateStory(
        prompt: String,
        durationMinutes: Int,
        characters: List<CharacterEntity>,
        projectId: Long
    ): StoryScript {
        val lowerPrompt = prompt.lowercase(Locale.ROOT)
        
        // Detect genre
        val genre = when {
            lowerPrompt.contains("zombie") || lowerPrompt.contains("horror") -> "Japanese Horror / Thriller"
            lowerPrompt.contains("cyberpunk") || lowerPrompt.contains("ninja") || lowerPrompt.contains("cyborg") -> "Cyberpunk Action / Sci-Fi"
            lowerPrompt.contains("magic") || lowerPrompt.contains("academy") || lowerPrompt.contains("sorcerer") -> "Supernatural Fantasy / Shonen"
            lowerPrompt.contains("mecha") || lowerPrompt.contains("titan") || lowerPrompt.contains("robot") -> "Mecha Warfare / Sci-Fi"
            else -> "Japanese 2D Action Anime"
        }

        // Generate Anime Title
        val animeTitle = when {
            lowerPrompt.contains("zombie") || lowerPrompt.contains("horror") -> "「渋谷ブラッドムーン」 SHIBUYA BLOOD MOON"
            lowerPrompt.contains("cyberpunk") || lowerPrompt.contains("ninja") -> "「電脳刃」 CYBER BLADE: NEO-TOKYO"
            lowerPrompt.contains("magic") || lowerPrompt.contains("academy") -> "「神聖魔法録」 ASTRAL CHRONICLES"
            lowerPrompt.contains("mecha") -> "「鉄騎前線」 IRON VALKYRIE"
            else -> "「最後の覚醒」 THE LAST AWAKENING"
        }

        // Generate Synopsis
        val synopsis = buildString {
            append("In a shattered, neon-drenched Tokyo, a sudden anomaly throws the metropolis into chaos. ")
            if (characters.isNotEmpty()) {
                val names = characters.joinToString(", ") { it.name }
                append("Heroes $names must combine their unique abilities, survive escalating threats, and confront the shadowy mastermind behind the crisis. ")
            } else {
                append("A band of unexpected allies must navigate betrayal, dark secrets, and high-octane battles to preserve tomorrow. ")
            }
            append("An intense cinematic 2D anime narrative voiced in Hindi with cinematic transitions.")
        }

        // Calculate scene count based on duration
        // For 5 min -> ~15 scenes, 10 min -> ~25 scenes, 15 min -> ~35-40 scenes, 20 min -> ~45 scenes, 30 min -> ~55 scenes
        val targetSceneCount = when (durationMinutes) {
            5 -> 15
            10 -> 25
            15 -> 35
            20 -> 45
            30 -> 55
            else -> 30
        }

        val fallbackNames = listOf("Kaito", "Rina", "Axel", "Momo", "Ren", "Narrator")
        val availableCharacters = if (characters.isNotEmpty()) characters else emptyList()

        val scenes = mutableListOf<SceneEntity>()

        // 4 Narrative Acts:
        // Act 1: The Gathering & Catalyst
        // Act 2: Rising Danger & First Skirmish
        // Act 3: Climax Confrontation
        // Act 4: Resolution & Cliffhanger

        for (i in 0 until targetSceneCount) {
            val sceneNumber = i + 1
            val progress = i.toFloat() / targetSceneCount.toFloat()

            val act = when {
                progress < 0.25f -> 1 // Introduction
                progress < 0.65f -> 2 // Escalation
                progress < 0.90f -> 3 // Climax
                else -> 4 // Finale
            }

            // Assign speaker
            val speaker: CharacterEntity? = if (availableCharacters.isNotEmpty()) {
                if (i % 6 == 0) null // Narrator
                else availableCharacters[(i % availableCharacters.size)]
            } else null

            val speakerName = speaker?.name ?: (if (i % 6 == 0) "Narrator (वर्णनकर्ता)" else fallbackNames[i % fallbackNames.size])
            val speakerId = speaker?.id

            val setting = BACKGROUND_THEMES[i % BACKGROUND_THEMES.size]
            val camera = CAMERA_MOTIONS[i % CAMERA_MOTIONS.size]
            val transition = TRANSITIONS[i % TRANSITIONS.size]
            val sfx = if (i % 3 == 0) SFX_LIST[i % (SFX_LIST.size - 1)] else "None"
            val bgm = when (act) {
                1 -> "Dramatic Strings"
                2 -> "Cyberpunk Synth"
                3 -> "Epic Battle"
                else -> "Melancholic Piano"
            }

            // Generate contextual Hindi dialogue & English translation based on Act & Speaker
            val (hindiLine, englishLine, emotion) = generateDialogue(act, speakerName, genre, i)

            val sceneTitle = "Scene $sceneNumber: ${getSceneSubtitle(act, i)}"

            scenes.add(
                SceneEntity(
                    projectId = projectId,
                    sceneIndex = i,
                    title = sceneTitle,
                    setting = setting,
                    durationSeconds = 5.0f,
                    cameraMotion = camera,
                    transition = transition,
                    dialogueHindi = hindiLine,
                    dialogueTranslation = englishLine,
                    speakerCharacterId = speakerId,
                    speakerName = speakerName,
                    emotion = emotion,
                    sfxType = sfx,
                    bgmTrack = bgm,
                    customImagePath = null,
                    backgroundTheme = setting
                )
            )
        }

        return StoryScript(
            title = animeTitle,
            genre = genre,
            synopsis = synopsis,
            scenes = scenes
        )
    }

    private fun getSceneSubtitle(act: Int, index: Int): String {
        return when (act) {
            1 -> listOf("Ominous Skies", "The Warning", "Sudden Outbreak", "Shadows Move", "First Contact")[index % 5]
            2 -> listOf("Shattered Barricade", "Neon Clash", "Unspoken Resolve", "Surrounded in Shibuya", "Power Unleashed")[index % 5]
            3 -> listOf("The Titan Awakens", "Limit Break Strike", "Clash of Blades", "Crimson Zenith", "The Final Stand")[index % 5]
            else -> listOf("Dawn Over Neo-Tokyo", "Aftermath Echoes", "A New Oath", "Shadows Linger")[index % 4]
        }
    }

    private fun generateDialogue(
        act: Int,
        speakerName: String,
        genre: String,
        index: Int
    ): Triple<String, String, String> {
        val isNarrator = speakerName.contains("Narrator") || speakerName.contains("वर्णनकर्ता")

        if (isNarrator) {
            return when (act) {
                1 -> Triple(
                    "टोक्यो की रात में अचानक एक खौफनाक सन्नाटा छा गया...",
                    "A chilling silence suddenly enveloped the Tokyo night...",
                    "Determined"
                )
                2 -> Triple(
                    "खतरा हर पल गहराता जा रहा था, और समय बहुत कम बचा था।",
                    "The danger deepened with every second, and time was running out.",
                    "Fierce"
                )
                3 -> Triple(
                    "यह सिर्फ जीने की नहीं, अपने वजूद को बचाने की आखिरी जंग थी!",
                    "This was not just survival, it was the final battle for their existence!",
                    "Fierce"
                )
                else -> Triple(
                    "धुएं के छंटते ही, एक नया सवेरा सामने आया। पर क्या यह सचमुच अंत था?",
                    "As the smoke cleared, a new dawn broke. But was this truly the end?",
                    "Sorrowful"
                )
            }
        }

        // Character Dialogues
        return when (act) {
            1 -> when (index % 4) {
                0 -> Triple(
                    "सब लोग पीछे हटो! ये सामान्य खतरा नहीं है!",
                    "Everyone stand back! This is no ordinary threat!",
                    "Shocked"
                )
                1 -> Triple(
                    "चाहे कुछ भी हो, मैं तुम सबको अकेला नहीं छोड़ूंगा।",
                    "No matter what happens, I will never leave you behind.",
                    "Determined"
                )
                2 -> Triple(
                    "मेरे सेंसर्स कुछ बहुत विशाल पकड़ रहे हैं... वो पास आ रहा है!",
                    "My sensors are picking up something massive... it's approaching!",
                    "Shocked"
                )
                else -> Triple(
                    "हथियार तैयार रखो! यहाँ से पीछे हटने का कोई रास्ता नहीं है।",
                    "Keep your weapons ready! There is no turning back from here.",
                    "Fierce"
                )
            }
            2 -> when (index % 4) {
                0 -> Triple(
                    "मेरी तलवार पर भरोसा रखो! मैं रास्ता साफ़ करता हूँ!",
                    "Trust my blade! I will clear the path!",
                    "Fierce"
                )
                1 -> Triple(
                    "इनकी संख्या लगातार बढ़ रही है! हमें छतों की तरफ बढ़ना होगा!",
                    "Their numbers keep swelling! We need to move toward the rooftops!",
                    "Determined"
                )
                2 -> Triple(
                    "तुम्हारी तकनीक और मेरी ताकत... हम मिलकर इन्हें रोक सकते हैं!",
                    "Your tech and my strength... together we can stop them!",
                    "Smug"
                )
                else -> Triple(
                    "सावधान! बाईं तरफ से हमला हो रहा है!",
                    "Watch out! Incoming attack from the left flank!",
                    "Shocked"
                )
            }
            3 -> when (index % 4) {
                0 -> Triple(
                    "अपनी पूरी ताकत झोंक दो! यह हमारा आखिरी प्रहार है!",
                    "Unleash everything you have! This is our final strike!",
                    "Fierce"
                )
                1 -> Triple(
                    "हम तब तक हार नहीं मानेंगे जब तक सांस बाकी है!",
                    "We won't surrender as long as breath remains!",
                    "Determined"
                )
                2 -> Triple(
                    "क्या तुम्हें सचमुच लगा कि तुम हमें हरा सकते हो?!",
                    "Did you truly think you could defeat us?!",
                    "Smug"
                )
                else -> Triple(
                    "आग का चक्र... अब भस्म हो जाओ!",
                    "Circle of flames... be incinerated now!",
                    "Fierce"
                )
            }
            else -> when (index % 4) {
                0 -> Triple(
                    "हमने कर दिखाया... हम सब ज़िंदा हैं।",
                    "We did it... all of us survived.",
                    "Smiling"
                )
                1 -> Triple(
                    "यह शहर कभी नहीं भूलेगा कि आज क्या हुआ था।",
                    "This city will never forget what transpired today.",
                    "Determined"
                )
                2 -> Triple(
                    "अगली लड़ाई के लिए... हम हमेशा तैयार रहेंगे।",
                    "For the next battle... we will always remain ready.",
                    "Determined"
                )
                else -> Triple(
                    "धन्यवाद दोस्तों, तुम सबके बिना यह मुमकिन नहीं था।",
                    "Thank you everyone, without you all this wouldn't be possible.",
                    "Smiling"
                )
            }
        }
    }
}
