package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.audio.HindiVoiceEngine
import com.example.data.local.VikasAnimeDatabase
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SceneEntity
import com.example.data.repository.AnimeStudioRepository
import com.example.generator.AnimeStoryEngine
import com.example.generator.CharacterCropper
import com.example.video.Mp4VideoExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class AnimeStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AnimeStudioRepository
    val voiceEngine: HindiVoiceEngine = HindiVoiceEngine(application)

    init {
        val db = VikasAnimeDatabase.getDatabase(application)
        repository = AnimeStudioRepository(db.projectDao(), db.characterDao(), db.sceneDao())
    }

    val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _activeProject = MutableStateFlow<ProjectEntity?>(null)
    val activeProject: StateFlow<ProjectEntity?> = _activeProject.asStateFlow()

    private val _activeCharacters = MutableStateFlow<List<CharacterEntity>>(emptyList())
    val activeCharacters: StateFlow<List<CharacterEntity>> = _activeCharacters.asStateFlow()

    private val _activeScenes = MutableStateFlow<List<SceneEntity>>(emptyList())
    val activeScenes: StateFlow<List<SceneEntity>> = _activeScenes.asStateFlow()

    // Character Sheet & Cropper State
    private val _currentSheetPath = MutableStateFlow<String?>(null)
    val currentSheetPath: StateFlow<String?> = _currentSheetPath.asStateFlow()

    private val _currentSheetBitmap = MutableStateFlow<Bitmap?>(null)
    val currentSheetBitmap: StateFlow<Bitmap?> = _currentSheetBitmap.asStateFlow()

    private val _selectedCropIndex = MutableStateFlow(0)
    val selectedCropIndex: StateFlow<Int> = _selectedCropIndex.asStateFlow()

    // Cinema Player State
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSceneIndex = MutableStateFlow(0)
    val currentSceneIndex: StateFlow<Int> = _currentSceneIndex.asStateFlow()

    private val _sceneProgress = MutableStateFlow(0f)
    val sceneProgress: StateFlow<Float> = _sceneProgress.asStateFlow()

    private var playbackJob: Job? = null

    // Video Export State
    sealed class ExportState {
        object Idle : ExportState()
        data class Exporting(val progress: Float, val currentScene: Int, val totalScenes: Int, val message: String) : ExportState()
        data class Finished(val videoFile: File) : ExportState()
        data class Error(val message: String) : ExportState()
    }

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    // Generation state indicator
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    init {
        // Automatically check or load default sample project
        viewModelScope.launch {
            val projects = repository.allProjects.firstOrNull()
            if (!projects.isNullOrEmpty()) {
                selectProject(projects.first().id)
            } else {
                createDefaultSampleProject()
            }
        }
    }

    fun selectProject(projectId: Long) {
        viewModelScope.launch {
            val project = repository.getProject(projectId)
            _activeProject.value = project
            if (project != null) {
                // Collect characters and scenes
                repository.getCharactersForProject(project.id).collect { characters ->
                    _activeCharacters.value = characters
                }
            }
        }
        viewModelScope.launch {
            _activeProject.value?.let { project ->
                repository.getScenesForProject(project.id).collect { scenes ->
                    _activeScenes.value = scenes
                }
            }
        }
    }

    fun createDefaultSampleProject() {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            val app = getApplication<Application>()

            // Ensure sample character sheet exists in private storage
            val sampleSheetPath = CharacterCropper.copyResourceToFile(
                app,
                R.drawable.sample_character_sheet,
                "default_sample_character_sheet.jpg"
            )
            _currentSheetPath.value = sampleSheetPath
            val sheetBitmap = CharacterCropper.loadBitmap(app, sampleSheetPath)
            _currentSheetBitmap.value = sheetBitmap

            val project = ProjectEntity(
                title = "「渋谷ブラッドムーン」 SHIBUYA BLOOD MOON",
                prompt = "Create a 15-minute Japanese zombie horror anime in Tokyo with 5 main characters.",
                durationMinutes = 15,
                totalScenes = 35,
                genre = "Japanese Horror / Thriller",
                synopsis = "A supernatural blood moon hangs over Tokyo as mutated creatures overrun Shibuya. Five high school comrades must fight their way through underground train tunnels and neon alleys to reach the Tokyo Tower beacon."
            )
            val projectId = repository.saveProject(project)
            val savedProject = project.copy(id = projectId)
            _activeProject.value = savedProject

            // Create 5 characters from default sheet
            val cropBoxes = CharacterCropper.getDefaultSlices(5)
            val characterDefs = listOf(
                CharacterEntity(
                    projectId = projectId,
                    name = "Kaito (काइटो)",
                    age = 17,
                    gender = "Male",
                    personality = "Brave, impulsive hero",
                    hairColor = "Spiky Crimson",
                    hairstyle = "Spiky Anime",
                    eyeColor = "Fiery Gold",
                    clothing = "Crimson Battle Jacket & Black Pants",
                    height = "176 cm",
                    specialAbilities = "Flame Katana Cleave",
                    storyRole = "Protagonist",
                    voiceType = "Hero Deep Male"
                ),
                CharacterEntity(
                    projectId = projectId,
                    name = "Rina (रीना)",
                    age = 18,
                    gender = "Female",
                    personality = "Calm, master swordswoman",
                    hairColor = "Silver White",
                    hairstyle = "Long High Ponytail",
                    eyeColor = "Piercing Blue",
                    clothing = "Cyber Tunic & Combat Skirt",
                    height = "168 cm",
                    specialAbilities = "Frost Blade Dance",
                    storyRole = "Rival",
                    voiceType = "Soft Female"
                ),
                CharacterEntity(
                    projectId = projectId,
                    name = "Axel (एक्सेल)",
                    age = 19,
                    gender = "Male",
                    personality = "Cynical genius hacker",
                    hairColor = "Neon Green",
                    hairstyle = "Undercut with Goggles",
                    eyeColor = "Emerald Cyber",
                    clothing = "Tactical Trenchcoat & Data Gauntlet",
                    height = "182 cm",
                    specialAbilities = "Neural EMP Overload",
                    storyRole = "Mentor",
                    voiceType = "Dramatic Villain"
                ),
                CharacterEntity(
                    projectId = projectId,
                    name = "Momo (मोमो)",
                    age = 16,
                    gender = "Female",
                    personality = "Energetic, kind-hearted mage",
                    hairColor = "Pastel Pink",
                    hairstyle = "Twin Pigtails",
                    eyeColor = "Amethyst Violet",
                    clothing = "Magic Academy Coat",
                    height = "156 cm",
                    specialAbilities = "Astral Barrier & Healing",
                    storyRole = "Supporting",
                    voiceType = "Soft Female"
                ),
                CharacterEntity(
                    projectId = projectId,
                    name = "Ren (रेन)",
                    age = 20,
                    gender = "Male",
                    personality = "Silent shadow assassin",
                    hairColor = "Midnight Black",
                    hairstyle = "Messy Shaggy",
                    eyeColor = "Crimson Eyes",
                    clothing = "Shadow Shinobi Cloak",
                    height = "179 cm",
                    specialAbilities = "Shadow Step & Void Daggers",
                    storyRole = "Antagonist",
                    voiceType = "Hero Deep Male"
                )
            )

            val charactersWithImages = characterDefs.mapIndexed { idx, char ->
                val box = cropBoxes.getOrElse(idx) { cropBoxes[0] }
                val croppedPath = if (sheetBitmap != null) {
                    CharacterCropper.cropAndSaveCharacter(app, sheetBitmap, box, char.name)
                } else null
                char.copy(croppedImagePath = croppedPath, sheetImagePath = sampleSheetPath)
            }

            repository.saveCharacters(charactersWithImages)
            val updatedCharacters = repository.getCharactersList(projectId)
            _activeCharacters.value = updatedCharacters

            // Generate Scenes
            val storyScript = AnimeStoryEngine.generateStory(
                prompt = project.prompt,
                durationMinutes = project.durationMinutes,
                characters = updatedCharacters,
                projectId = projectId
            )

            repository.saveScenes(storyScript.scenes)
            val updatedScenes = repository.getScenesList(projectId)
            _activeScenes.value = updatedScenes

            _isGenerating.value = false
        }
    }

    /**
     * User submits a new story prompt with desired duration
     */
    fun generateNewAnimeProject(prompt: String, durationMinutes: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGenerating.value = true
            val app = getApplication<Application>()

            val existingCharacters = _activeCharacters.value

            val project = ProjectEntity(
                title = "Anime: ${prompt.take(30)}...",
                prompt = prompt,
                durationMinutes = durationMinutes,
                totalScenes = when (durationMinutes) {
                    5 -> 15
                    10 -> 25
                    15 -> 35
                    20 -> 45
                    else -> 55
                }
            )

            val projectId = repository.saveProject(project)
            val savedProject = project.copy(id = projectId)
            _activeProject.value = savedProject

            // If no characters yet, copy default sample characters
            val charactersToUse = if (existingCharacters.isEmpty()) {
                val sampleSheetPath = CharacterCropper.copyResourceToFile(
                    app,
                    R.drawable.sample_character_sheet,
                    "default_sample_character_sheet.jpg"
                )
                _currentSheetPath.value = sampleSheetPath
                val sheetBitmap = CharacterCropper.loadBitmap(app, sampleSheetPath)
                _currentSheetBitmap.value = sheetBitmap

                val cropBoxes = CharacterCropper.getDefaultSlices(5)
                val defs = listOf(
                    CharacterEntity(projectId = projectId, name = "Kaito", voiceType = "Hero Deep Male"),
                    CharacterEntity(projectId = projectId, name = "Rina", voiceType = "Soft Female"),
                    CharacterEntity(projectId = projectId, name = "Axel", voiceType = "Dramatic Villain"),
                    CharacterEntity(projectId = projectId, name = "Momo", voiceType = "Soft Female"),
                    CharacterEntity(projectId = projectId, name = "Ren", voiceType = "Energetic Youth")
                ).mapIndexed { idx, char ->
                    val cropped = if (sheetBitmap != null) {
                        CharacterCropper.cropAndSaveCharacter(app, sheetBitmap, cropBoxes[idx], char.name)
                    } else null
                    char.copy(croppedImagePath = cropped, sheetImagePath = sampleSheetPath)
                }
                repository.saveCharacters(defs)
                repository.getCharactersList(projectId)
            } else {
                // Copy existing characters to new project
                val cloned = existingCharacters.map { it.copy(id = 0, projectId = projectId) }
                repository.saveCharacters(cloned)
                repository.getCharactersList(projectId)
            }

            _activeCharacters.value = charactersToUse

            // Generate complete story & scenes
            val script = AnimeStoryEngine.generateStory(
                prompt = prompt,
                durationMinutes = durationMinutes,
                characters = charactersToUse,
                projectId = projectId
            )

            repository.saveScenes(script.scenes)
            val finalScenes = repository.getScenesList(projectId)
            _activeScenes.value = finalScenes

            _activeProject.value = savedProject.copy(
                title = script.title,
                genre = script.genre,
                synopsis = script.synopsis,
                totalScenes = script.scenes.size
            )
            repository.saveProject(_activeProject.value!!)

            _isGenerating.value = false
            _currentSceneIndex.value = 0
            _sceneProgress.value = 0f
        }
    }

    /**
     * User uploads or selects a character sheet image Uri
     */
    fun onCharacterSheetUploaded(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val savedPath = CharacterCropper.saveSheetImage(app, uri)
            if (savedPath != null) {
                _currentSheetPath.value = savedPath
                val bmp = CharacterCropper.loadBitmap(app, savedPath)
                _currentSheetBitmap.value = bmp
            }
        }
    }

    /**
     * Sets crop box index or crops character
     */
    fun cropCharacterFromSheet(
        box: CharacterCropper.CropBox,
        name: String,
        role: String,
        voiceType: String,
        personality: String,
        abilities: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val project = _activeProject.value ?: return@launch
            val app = getApplication<Application>()
            val bmp = _currentSheetBitmap.value ?: return@launch

            val croppedPath = CharacterCropper.cropAndSaveCharacter(app, bmp, box, name)
            val character = CharacterEntity(
                projectId = project.id,
                name = name,
                storyRole = role,
                voiceType = voiceType,
                personality = personality,
                specialAbilities = abilities,
                croppedImagePath = croppedPath,
                sheetImagePath = _currentSheetPath.value
            )
            repository.saveCharacter(character)
            _activeCharacters.value = repository.getCharactersList(project.id)
        }
    }

    fun updateCharacter(character: CharacterEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveCharacter(character)
            _activeProject.value?.let { p ->
                _activeCharacters.value = repository.getCharactersList(p.id)
            }
        }
    }

    fun deleteCharacter(character: CharacterEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCharacter(character)
            _activeProject.value?.let { p ->
                _activeCharacters.value = repository.getCharactersList(p.id)
            }
        }
    }

    // Timeline scene editing
    fun updateScene(scene: SceneEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateScene(scene)
            _activeProject.value?.let { p ->
                _activeScenes.value = repository.getScenesList(p.id)
            }
        }
    }

    fun deleteScene(scene: SceneEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteScene(scene)
            _activeProject.value?.let { p ->
                val list = repository.getScenesList(p.id)
                _activeScenes.value = list
                if (_currentSceneIndex.value >= list.size) {
                    _currentSceneIndex.value = (list.size - 1).coerceAtLeast(0)
                }
            }
        }
    }

    fun addSceneAfter(index: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val project = _activeProject.value ?: return@launch
            val scenes = _activeScenes.value.toMutableList()
            val newScene = SceneEntity(
                projectId = project.id,
                sceneIndex = index + 1,
                title = "Scene ${scenes.size + 1}: Custom Scene",
                setting = "Cyberpunk Neon Alley",
                durationSeconds = 5.0f,
                cameraMotion = "Ken Burns Zoom In",
                transition = "Flash Cut",
                dialogueHindi = "यह नया दृश्य शुरू हो रहा है!",
                dialogueTranslation = "This new scene begins now!",
                speakerName = _activeCharacters.value.firstOrNull()?.name ?: "Narrator",
                emotion = "Determined",
                sfxType = "Katana Slash",
                bgmTrack = "Epic Battle"
            )
            scenes.add((index + 1).coerceAtMost(scenes.size), newScene)
            // Re-index
            val reindexed = scenes.mapIndexed { idx, s -> s.copy(sceneIndex = idx) }
            repository.replaceAllScenes(project.id, reindexed)
            _activeScenes.value = reindexed
        }
    }

    fun moveScene(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val project = _activeProject.value ?: return@launch
            val scenes = _activeScenes.value.toMutableList()
            if (fromIndex in scenes.indices && toIndex in scenes.indices) {
                val item = scenes.removeAt(fromIndex)
                scenes.add(toIndex, item)
                val reindexed = scenes.mapIndexed { idx, s -> s.copy(sceneIndex = idx) }
                repository.replaceAllScenes(project.id, reindexed)
                _activeScenes.value = reindexed
            }
        }
    }

    // Cinema Playback controls
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun startPlayback() {
        _isPlaying.value = true
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val scenes = _activeScenes.value
            if (scenes.isEmpty()) {
                _isPlaying.value = false
                return@launch
            }

            while (_isPlaying.value) {
                val currentIndex = _currentSceneIndex.value
                val scene = scenes.getOrNull(currentIndex) ?: break

                // Trigger Hindi Voice dialogue and SFX
                val character = _activeCharacters.value.firstOrNull { it.id == scene.speakerCharacterId || it.name == scene.speakerName }
                val voiceType = character?.voiceType ?: "Hero Deep Male"

                withContext(Dispatchers.Main) {
                    voiceEngine.playSfx(scene.sfxType)
                    voiceEngine.speakDialogue(scene.dialogueHindi, voiceType)
                }

                val durationMs = (scene.durationSeconds * 1000).toLong()
                val steps = 40
                val stepInterval = durationMs / steps

                for (step in 0..steps) {
                    if (!_isPlaying.value) break
                    _sceneProgress.value = step.toFloat() / steps.toFloat()
                    delay(stepInterval)
                }

                if (!_isPlaying.value) break

                // Move to next scene
                if (currentIndex < scenes.size - 1) {
                    _currentSceneIndex.value = currentIndex + 1
                    _sceneProgress.value = 0f
                } else {
                    // Reached end
                    _isPlaying.value = false
                    _currentSceneIndex.value = 0
                    _sceneProgress.value = 0f
                    break
                }
            }
        }
    }

    fun pausePlayback() {
        _isPlaying.value = false
        playbackJob?.cancel()
        voiceEngine.stop()
    }

    fun seekToScene(index: Int) {
        val scenes = _activeScenes.value
        if (index in scenes.indices) {
            _currentSceneIndex.value = index
            _sceneProgress.value = 0f
            if (_isPlaying.value) {
                startPlayback()
            }
        }
    }

    // Export MP4 Video
    fun exportVideo(resolution: Mp4VideoExporter.Resolution = Mp4VideoExporter.Resolution.HD_720P) {
        viewModelScope.launch {
            val scenes = _activeScenes.value
            val characters = _activeCharacters.value
            val app = getApplication<Application>()

            _exportState.value = ExportState.Exporting(0f, 1, scenes.size, "Starting MP4 Render...")

            val file = Mp4VideoExporter.exportAnimeVideo(
                context = app,
                scenes = scenes,
                characters = characters,
                resolution = resolution
            ) { progress, currentScene, totalScenes, message ->
                _exportState.value = ExportState.Exporting(progress, currentScene, totalScenes, message)
            }

            if (file != null && file.exists()) {
                _exportState.value = ExportState.Finished(file)
            } else {
                _exportState.value = ExportState.Error("Failed to encode MP4 video. Check storage space.")
            }
        }
    }

    fun dismissExportDialog() {
        _exportState.value = ExportState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.shutdown()
        playbackJob?.cancel()
    }
}
