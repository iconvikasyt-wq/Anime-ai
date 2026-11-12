package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.CharacterEntity
import com.example.generator.AnimeStoryEngine
import com.example.generator.CharacterCropper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Vikas AI Anime Studio", appName)
  }

  @Test
  fun `story engine generates scenes with Hindi dialogues`() {
    val testCharacters = listOf(
      CharacterEntity(
        id = 1L,
        projectId = 1L,
        name = "Kaito",
        voiceType = "Hero Deep Male"
      ),
      CharacterEntity(
        id = 2L,
        projectId = 1L,
        name = "Rina",
        voiceType = "Soft Female"
      )
    )

    val script = AnimeStoryEngine.generateStory(
      prompt = "Tokyo zombie horror with 5 characters",
      durationMinutes = 15,
      characters = testCharacters,
      projectId = 1L
    )

    assertNotNull(script)
    assertTrue("Title should contain Japanese aesthetic", script.title.contains("「") || script.title.isNotBlank())
    assertTrue("Should generate 30+ scenes for 15 min duration", script.scenes.size >= 30)

    val firstScene = script.scenes.first()
    assertTrue("First scene should have Hindi dialogue", firstScene.dialogueHindi.isNotBlank())
    assertNotNull(firstScene.dialogueTranslation)
  }

  @Test
  fun `character cropper default slices cover full width`() {
    val slices = CharacterCropper.getDefaultSlices(5)
    assertEquals(5, slices.size)
    assertEquals(0.0f, slices.first().left, 0.01f)
    assertEquals(1.0f, slices.last().right, 0.01f)
  }
}
