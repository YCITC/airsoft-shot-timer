package com.example.airsoftshottimer.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ShotRecord::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shotDao(): ShotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shot_timer_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
