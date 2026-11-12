package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.entity.CharacterEntity
import com.example.generator.CharacterCropper
import com.example.ui.theme.AnimeBorderGlow
import com.example.ui.theme.AnimeCardElevated
import com.example.ui.theme.AnimeCardSurface
import com.example.ui.theme.AnimeDarkBg
import com.example.ui.theme.AnimeRed
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AnimeStudioViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersScreen(
    viewModel: AnimeStudioViewModel,
    modifier: Modifier = Modifier
) {
    val characters by viewModel.activeCharacters.collectAsStateWithLifecycle()
    val sheetPath by viewModel.currentSheetPath.collectAsStateWithLifecycle()
    val sheetBitmap by viewModel.currentSheetBitmap.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Android zero-permission photo picker for character sheet
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.onCharacterSheetUploaded(uri)
        }
    }

    var showCropDialog by remember { mutableStateOf(false) }
    var editingCharacter by remember { mutableStateOf<CharacterEntity?>(null) }

    // Cropping slice state
    var selectedSliceIndex by remember { mutableIntStateOf(0) }
    var sliceLeftRatio by remember { mutableFloatStateOf(0.0f) }
    var sliceRightRatio by remember { mutableFloatStateOf(0.20f) }

    // Form inputs for new character crop
    var charName by remember { mutableStateOf("New Character") }
    var charRole by remember { mutableStateOf("Protagonist") }
    var charVoice by remember { mutableStateOf("Hero Deep Male") }
    var charAge by remember { mutableStateOf("17") }
    var charPersonality by remember { mutableStateOf("Determined Hero") }
    var charHair by remember { mutableStateOf("Spiky Anime") }
    var charEyes by remember { mutableStateOf("Fiery Gold") }
    var charClothing by remember { mutableStateOf("High-tech Battle Jacket") }
    var charAbilities by remember { mutableStateOf("Katana Flame Burst") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(AnimeDarkBg)
            .padding(16.dp)
            .testTag("characters_screen")
    ) {
        // Top Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.People,
                    contentDescription = null,
                    tint = NeonPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "MY ANIME CHARACTERS",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Upload 1 image sheet • Crop multiple characters • Lock consistency in all scenes",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Upload & Slicing Control Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AnimeCardSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AnimeBorderGlow, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "CHARACTER SHEET CROPPING SYSTEM",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select a character lineup sheet containing up to 5 anime characters side-by-side:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Buttons: Upload sheet or Load Sample
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("upload_sheet_button")
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload Sheet", fontSize = 11.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.createDefaultSampleProject()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
                            border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("sample_sheet_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset 5 Sample", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sheet Preview & Slicing Grid
                    if (sheetBitmap != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, AnimeBorderGlow, RoundedCornerShape(8.dp))
                                .background(Color.Black)
                        ) {
                            Image(
                                bitmap = sheetBitmap!!.asImageBitmap(),
                                contentDescription = "Active Character Sheet",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Bounding Box Overlay for current slice
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = (sheetBitmap!!.width * 0.1f * (sliceLeftRatio)).dp,
                                        end = 0.dp
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick 5-slice selector buttons
                        Text(
                            text = "SELECT CHARACTER POSITION TO CROP:",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val names = listOf("Pos 1", "Pos 2", "Pos 3", "Pos 4", "Pos 5")
                            for (i in 0..4) {
                                val isSelected = selectedSliceIndex == i
                                val left = i * 0.20f
                                val right = (i + 1) * 0.20f
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) NeonPink else AnimeCardElevated)
                                        .border(1.dp, if (isSelected) Color.White else AnimeBorderGlow, RoundedCornerShape(6.dp))
                                        .clickable {
                                            selectedSliceIndex = i
                                            sliceLeftRatio = left
                                            sliceRightRatio = right
                                            charName = when (i) {
                                                0 -> "Kaito"
                                                1 -> "Rina"
                                                2 -> "Axel"
                                                3 -> "Momo"
                                                else -> "Ren"
                                            }
                                        }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = names[i],
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                showCropDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("crop_character_button")
                        ) {
                            Icon(Icons.Default.Crop, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Crop & Create Character Profile", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // Fallback placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AnimeCardElevated),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No sheet loaded. Click 'Reset 5 Sample' or 'Upload Sheet'.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
        }

        // Section Title
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ACTIVE ROSTER (${characters.size} CHARACTERS)",
                    color = NeonPurpleGlow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Locked Consistent Faces",
                    color = NeonCyan,
                    fontSize = 10.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Characters Roster List
        if (characters.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AnimeCardSurface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No characters yet. Crop characters above to build your team!",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(characters, key = { it.id }) { character ->
                CharacterCard(
                    character = character,
                    onTestVoice = {
                        viewModel.voiceEngine.speakDialogue(
                            "नमस्ते! मैं ${character.name} हूँ। हमारी लड़ाई जारी रहेगी!",
                            character.voiceType
                        )
                    },
                    onEdit = { editingCharacter = character },
                    onDelete = { viewModel.deleteCharacter(character) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Dialog for Cropping New Character
    if (showCropDialog) {
        AlertDialog(
            onDismissRequest = { showCropDialog = false },
            title = {
                Text(
                    text = "New Character Profile",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    Text(
                        text = "Define character details to ensure consistency across all anime scenes:",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = charName,
                        onValueChange = { charName = it },
                        label = { Text("Character Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = charRole,
                        onValueChange = { charRole = it },
                        label = { Text("Story Role (Protagonist, Rival, Mentor...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = charVoice,
                        onValueChange = { charVoice = it },
                        label = { Text("Hindi Voice Type (Hero Deep Male, Soft Female...)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = charPersonality,
                        onValueChange = { charPersonality = it },
                        label = { Text("Personality & Traits") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = charAbilities,
                        onValueChange = { charAbilities = it },
                        label = { Text("Special Abilities") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cropBox = CharacterCropper.CropBox(
                            left = sliceLeftRatio,
                            top = 0.05f,
                            right = sliceRightRatio,
                            bottom = 0.95f
                        )
                        viewModel.cropCharacterFromSheet(
                            box = cropBox,
                            name = charName,
                            role = charRole,
                            voiceType = charVoice,
                            personality = charPersonality,
                            abilities = charAbilities
                        )
                        showCropDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Save Character")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCropDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AnimeCardSurface
        )
    }

    // Dialog for Editing Existing Character
    if (editingCharacter != null) {
        val target = editingCharacter!!
        var editName by remember(target) { mutableStateOf(target.name) }
        var editRole by remember(target) { mutableStateOf(target.storyRole) }
        var editVoice by remember(target) { mutableStateOf(target.voiceType) }
        var editPersonality by remember(target) { mutableStateOf(target.personality) }
        var editAbilities by remember(target) { mutableStateOf(target.specialAbilities) }
        var editClothing by remember(target) { mutableStateOf(target.clothing) }

        AlertDialog(
            onDismissRequest = { editingCharacter = null },
            title = {
                Text(
                    text = "Edit Character: ${target.name}",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editRole,
                        onValueChange = { editRole = it },
                        label = { Text("Story Role") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editVoice,
                        onValueChange = { editVoice = it },
                        label = { Text("Hindi Voice Type") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editPersonality,
                        onValueChange = { editPersonality = it },
                        label = { Text("Personality") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editClothing,
                        onValueChange = { editClothing = it },
                        label = { Text("Clothing / Outfit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editAbilities,
                        onValueChange = { editAbilities = it },
                        label = { Text("Special Abilities") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateCharacter(
                            target.copy(
                                name = editName,
                                storyRole = editRole,
                                voiceType = editVoice,
                                personality = editPersonality,
                                clothing = editClothing,
                                specialAbilities = editAbilities
                            )
                        )
                        editingCharacter = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingCharacter = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = AnimeCardSurface
        )
    }
}

@Composable
private fun CharacterCard(
    character: CharacterEntity,
    onTestVoice: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = AnimeCardSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AnimeBorderGlow, RoundedCornerShape(12.dp))
            .testTag("character_card_${character.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cropped Portrait Bitmap
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.5.dp, NeonPurple, RoundedCornerShape(10.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (character.croppedImagePath != null) {
                    val file = File(character.croppedImagePath)
                    if (file.exists()) {
                        AsyncImage(
                            model = file,
                            contentDescription = character.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(Icons.Default.People, contentDescription = null, tint = TextSecondary)
                    }
                } else {
                    Icon(Icons.Default.People, contentDescription = null, tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = character.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(NeonPink.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, NeonPink, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = character.storyRole,
                            color = NeonPink,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Traits: ${character.personality}",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Text(
                    text = "Abilities: ${character.specialAbilities}",
                    color = NeonCyan,
                    fontSize = 10.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Voice button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AnimeCardElevated)
                        .clickable { onTestVoice() }
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.VolumeUp, contentDescription = "Test Voice", tint = NeonPurpleGlow, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hindi Voice: ${character.voiceType}",
                        color = NeonPurpleGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Edit & Delete actions
            Column {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Character", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Character", tint = AnimeRed.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
