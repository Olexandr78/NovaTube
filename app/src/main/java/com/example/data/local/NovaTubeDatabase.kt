package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "preference_tags")
data class PreferenceTagEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tag: String,
    val category: String,
    val weight: Int, // -100 for blocked, 1..100 for positive affinity
    val isBlocked: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val author: String,
    val category: String,
    val tagsCsv: String,
    val thumbnailUrl: String,
    val streamUrl: String,
    val watchedSeconds: Int,
    val durationSeconds: Int,
    val isLiked: Boolean = false,
    val isBookmarked: Boolean = false,
    val adsSkippedCount: Int = 2,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloaded_videos")
data class DownloadedVideoEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val author: String,
    val thumbnailUrl: String,
    val remoteStreamUrl: String,
    val localFilePath: String,
    val quality: String,
    val totalBytes: Long,
    val downloadedBytes: Long,
    val speedKbps: Int,
    val status: String, // "DOWNLOADING", "COMPLETED", "FAILED"
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface NovaTubeDao {
    // --- Preference Tags ---
    @Query("SELECT * FROM preference_tags ORDER BY isBlocked ASC, weight DESC, updatedAt DESC")
    fun getAllPreferenceTags(): Flow<List<PreferenceTagEntity>>

    @Query("SELECT * FROM preference_tags ORDER BY isBlocked ASC, weight DESC")
    suspend fun getPreferenceTagsSnapshot(): List<PreferenceTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferenceTag(tag: PreferenceTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferenceTags(tags: List<PreferenceTagEntity>)

    @Query("UPDATE preference_tags SET weight = :weight, isBlocked = :isBlocked, updatedAt = :now WHERE id = :id")
    suspend fun updateTagWeight(id: Int, weight: Int, isBlocked: Boolean, now: Long = System.currentTimeMillis())

    @Query("DELETE FROM preference_tags WHERE id = :id")
    suspend fun deletePreferenceTag(id: Int)

    @Query("DELETE FROM preference_tags")
    suspend fun clearAllPreferenceTags()

    @Query("SELECT COUNT(*) FROM preference_tags")
    suspend fun getPreferenceTagCount(): Int

    // --- Watch History & Bookmarks ---
    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC")
    fun getWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC LIMIT 20")
    suspend fun getRecentWatchHistorySnapshot(): List<WatchHistoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistory(entry: WatchHistoryEntity)

    @Query("SELECT * FROM watch_history WHERE videoId = :videoId LIMIT 1")
    suspend fun getHistoryById(videoId: String): WatchHistoryEntity?

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()

    // --- Downloaded Videos ---
    @Query("SELECT * FROM downloaded_videos ORDER BY timestamp DESC")
    fun getDownloadedVideos(): Flow<List<DownloadedVideoEntity>>

    @Query("SELECT * FROM downloaded_videos WHERE videoId = :videoId LIMIT 1")
    suspend fun getDownloadById(videoId: String): DownloadedVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDownload(download: DownloadedVideoEntity)

    @Query("DELETE FROM downloaded_videos WHERE videoId = :videoId")
    suspend fun deleteDownloadById(videoId: String)
}

@Database(
    entities = [
        PreferenceTagEntity::class,
        WatchHistoryEntity::class,
        DownloadedVideoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NovaTubeDatabase : RoomDatabase() {
    abstract fun dao(): NovaTubeDao

    companion object {
        @Volatile
        private var INSTANCE: NovaTubeDatabase? = null

        fun getInstance(context: Context): NovaTubeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NovaTubeDatabase::class.java,
                    "novatube_local.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
