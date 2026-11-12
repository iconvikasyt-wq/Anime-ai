package com.example.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object CharacterCropper {

    /**
     * Represents a normalized bounding box (0.0f .. 1.0f) inside a character sheet image
     */
    data class CropBox(
        val left: Float,
        val top: Float,
        val right: Float,
        val bottom: Float
    )

    /**
     * Splits an image horizontally into [count] character slices by default
     */
    fun getDefaultSlices(count: Int = 5): List<CropBox> {
        val boxes = mutableListOf<CropBox>()
        val step = 1.0f / count.coerceAtLeast(1)
        for (i in 0 until count) {
            val left = (i * step).coerceIn(0f, 1f)
            val right = ((i + 1) * step).coerceIn(0f, 1f)
            boxes.add(CropBox(left = left, top = 0.05f, right = right, bottom = 0.95f))
        }
        return boxes
    }

    /**
     * Decodes a Bitmap from an Android Uri or local file path
     */
    fun loadBitmap(context: Context, uriOrPath: String): Bitmap? {
        return try {
            if (uriOrPath.startsWith("content://") || uriOrPath.startsWith("file://")) {
                val uri = Uri.parse(uriOrPath)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } else {
                val file = File(uriOrPath)
                if (file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crops a character region from a source bitmap and saves it as a PNG file
     */
    fun cropAndSaveCharacter(
        context: Context,
        sourceBitmap: Bitmap,
        cropBox: CropBox,
        characterName: String
    ): String? {
        return try {
            val srcW = sourceBitmap.width
            val srcH = sourceBitmap.height

            val leftPx = (cropBox.left.coerceIn(0f, 1f) * srcW).toInt()
            val topPx = (cropBox.top.coerceIn(0f, 1f) * srcH).toInt()
            val rightPx = (cropBox.right.coerceIn(0f, 1f) * srcW).toInt()
            val bottomPx = (cropBox.bottom.coerceIn(0f, 1f) * srcH).toInt()

            val cropW = (rightPx - leftPx).coerceAtLeast(10)
            val cropH = (bottomPx - topPx).coerceAtLeast(10)

            val safeX = leftPx.coerceIn(0, srcW - 1)
            val safeY = topPx.coerceIn(0, srcH - 1)
            val safeW = cropW.coerceAtMost(srcW - safeX)
            val safeH = cropH.coerceAtMost(srcH - safeY)

            val cropped = Bitmap.createBitmap(sourceBitmap, safeX, safeY, safeW, safeH)

            val dir = File(context.filesDir, "characters")
            if (!dir.exists()) dir.mkdirs()

            val sanitizedName = characterName.lowercase().replace("\\s+".toRegex(), "_")
            val fileName = "char_${sanitizedName}_${UUID.randomUUID().toString().take(6)}.png"
            val destFile = File(dir, fileName)

            FileOutputStream(destFile).use { out ->
                cropped.compress(Bitmap.CompressFormat.PNG, 95, out)
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copies a Uri content into a permanent project sheet file
     */
    fun saveSheetImage(context: Context, uri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "sheets")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "sheet_${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Copies drawable resource (such as sample_character_sheet) to local file if needed
     */
    fun copyResourceToFile(context: Context, resId: Int, fileName: String): String {
        val dir = File(context.filesDir, "sheets")
        if (!dir.exists()) dir.mkdirs()
        val dest = File(dir, fileName)
        if (!dest.exists()) {
            context.resources.openRawResource(resId).use { input ->
                FileOutputStream(dest).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return dest.absolutePath
    }
}
