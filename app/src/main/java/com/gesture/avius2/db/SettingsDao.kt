package com.gesture.avius2.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SettingsDao {

    // Suspend puts must condition on running these methods Asynchronously
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg settings: SettingsEntity)

    @Query("SELECT * FROM settings LIMIT 1")
    suspend fun getAll(): SettingsEntity

    @Query("SELECT * FROM settings")
    fun getAllLive(): LiveData<SettingsEntity>

    @Query("DELETE FROM settings")
    suspend fun deleteAll()
}