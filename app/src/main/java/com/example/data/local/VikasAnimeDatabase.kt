package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.CharacterDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.SceneDao
import com.example.data.local.entity.CharacterEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.SceneEntity

@Database(
    entities = [
        ProjectEntity::class,
        CharacterEntity::class,
        SceneEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VikasAnimeDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun characterDao(): CharacterDao
    abstract fun sceneDao(): SceneDao

    companion object {
        @Volatile
        private var INSTANCE: VikasAnimeDatabase? = null

        fun getDatabase(context: Context): VikasAnimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VikasAnimeDatabase::class.java,
                    "vikas_anime_studio.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
