package com.chimera.zpqmxr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "payloads")
data class Payload(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val script: String,
    val timestamp: Long = System.currentTimeMillis()
)
