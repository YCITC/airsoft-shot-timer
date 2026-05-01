package com.example.airsoftshottimer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ShotDao {
    @Query("SELECT * FROM shot_records ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ShotRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ShotRecord)

    @Delete
    suspend fun delete(record: ShotRecord)
    
    @Query("SELECT * FROM shot_records ORDER BY time ASC LIMIT 1")
    fun getBestShot(): Flow<ShotRecord?>
}
