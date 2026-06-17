package com.chimera.zpqmxr.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Payload::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun payloadDao(): PayloadDao
}
