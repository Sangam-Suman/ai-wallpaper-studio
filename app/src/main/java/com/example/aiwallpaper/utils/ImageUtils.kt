package com.example.aiwallpaper.utils

import android.app.WallpaperManager
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun base64ToBitmap(base64: String): Bitmap? = runCatching {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }.getOrNull()

    fun loadBitmapFromPath(path: String): Bitmap? = runCatching {
        BitmapFactory.decodeFile(path)
    }.getOrNull()

    // Saves generated image to the app's private files directory for history display
    fun saveImageToInternalStorage(context: Context, base64: String, fileName: String): String? {
        val bitmap = base64ToBitmap(base64) ?: return null
        val dir = File(context.filesDir, "wallpapers").also { it.mkdirs() }
        val file = File(dir, "$fileName.jpg")
        return runCatching {
            FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
            file.absolutePath
        }.getOrNull()
    }

    // Saves to the public Pictures/AIWallpaper gallery folder
    fun saveToGallery(context: Context, imagePath: String, fileName: String): Uri? {
        val bitmap = loadBitmapFromPath(imagePath) ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/AIWallpaper")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                context.contentResolver.update(it, values, null, null)
            }
            uri
        } else {
            @Suppress("DEPRECATION")
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "AIWallpaper"
            ).also { it.mkdirs() }
            val file = File(dir, "$fileName.jpg")
            runCatching {
                FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it) }
                Uri.fromFile(file)
            }.getOrNull()
        }
    }

    fun setAsWallpaper(context: Context, imagePath: String): Boolean {
        val bitmap = loadBitmapFromPath(imagePath) ?: return false
        return runCatching {
            WallpaperManager.getInstance(context).setBitmap(bitmap)
            true
        }.getOrDefault(false)
    }
}
