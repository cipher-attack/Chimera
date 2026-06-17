package com.chimera.zpqmxr.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PayloadDao {
    @Query("SELECT * FROM payloads ORDER BY timestamp DESC")
    fun getAllPayloads(): Flow<List<Payload>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayload(payload: Payload)

    @Query("DELETE FROM payloads WHERE id = :id")
    suspend fun deletePayloadById(id: Int)
}
