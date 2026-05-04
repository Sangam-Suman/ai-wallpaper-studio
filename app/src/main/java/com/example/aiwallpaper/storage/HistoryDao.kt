package com.example.aiwallpaper.storage

import androidx.room.*
import com.example.aiwallpaper.data.model.WallpaperHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM wallpaper_history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<WallpaperHistory>>

    @Query("SELECT * FROM wallpaper_history WHERE id = :id")
    suspend fun getById(id: Long): WallpaperHistory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: WallpaperHistory): Long

    @Delete
    suspend fun delete(history: WallpaperHistory)

    @Query("DELETE FROM wallpaper_history")
    suspend fun deleteAll()
}
