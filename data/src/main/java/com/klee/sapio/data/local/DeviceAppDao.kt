package com.klee.sapio.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeviceAppDao {

    @Query("SELECT * FROM DeviceAppEntity")
    suspend fun getAll(): List<DeviceAppEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<DeviceAppEntity>)

    @Query("DELETE FROM DeviceAppEntity")
    suspend fun deleteAll()
}
