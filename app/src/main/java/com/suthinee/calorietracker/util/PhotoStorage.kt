package com.suthinee.calorietracker.util

import android.content.Context
import androidx.core.content.FileProvider
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** Saves meal photos under the app's private files dir so they persist offline with no external storage permission. */
object PhotoStorage {

    private val fileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS")

    private fun photosDir(context: Context): File =
        File(context.filesDir, "food_photos").apply { if (!exists()) mkdirs() }

    fun createNewPhotoFile(context: Context): File {
        val name = "meal_${LocalDateTime.now().format(fileNameFormatter)}.jpg"
        return File(photosDir(context), name)
    }

    fun uriForFile(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun deletePhoto(path: String?) {
        if (path.isNullOrBlank()) return
        val file = File(path)
        if (file.exists()) file.delete()
    }
}
