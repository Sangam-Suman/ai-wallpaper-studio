package com.example.aiwallpaper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallpaper_history")
data class WallpaperHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prompt: String,
    val style: String,
    val imagePath: String,
    val createdAt: Long = System.currentTimeMillis()
)
